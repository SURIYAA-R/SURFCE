package com.example.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState as MediaPlaybackState
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import com.example.data.local.WavyxDatabase
import com.example.data.model.PlaybackState
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioPlayerEngine private constructor(private val context: Context) {

    private val database = WavyxDatabase.getDatabase(context)
    private val songDao = database.songDao()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var mediaPlayer: MediaPlayer? = null
    private var syntheticTrack: AudioTrack? = null
    private var syntheticToneJob: Job? = null
    private var progressPollingJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private var resumeOnFocusGain = false

    private var mediaSession: MediaSession? = null
    private var isNoisyReceiverRegistered = false

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Headset unplugged or Bluetooth earbuds disconnected -> pause playback
                Log.d("AudioPlayerEngine", "Audio becoming noisy (headset disconnected) - pausing playback")
                pause()
            }
        }
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: AudioPlayerEngine? = null

        fun getInstance(context: Context): AudioPlayerEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AudioPlayerEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    init {
        setupMediaSession()
        startProgressAndWaveformUpdater()
    }

    fun getMediaSessionToken(): MediaSession.Token? = mediaSession?.sessionToken

    private fun setupMediaSession() {
        try {
            mediaSession = MediaSession(context, "SURFCE_AudioSession").apply {
                setFlags(
                    MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS
                )

                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() {
                        resume()
                    }

                    override fun onPause() {
                        pause()
                    }

                    override fun onSkipToNext() {
                        playNext()
                    }

                    override fun onSkipToPrevious() {
                        playPrevious()
                    }

                    override fun onStop() {
                        pause()
                    }

                    override fun onSeekTo(pos: Long) {
                        seekTo(pos)
                    }

                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        }

                        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_HEADSETHOOK,
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                    togglePlayPause()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                    resume()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                    pause()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                    playNext()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                    playPrevious()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_STOP -> {
                                    pause()
                                    return true
                                }
                            }
                        }
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }
                })

                isActive = true
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Failed to initialize MediaSession: ${e.message}")
        }
    }

    private fun updateMediaSessionState(isPlaying: Boolean, positionMs: Long) {
        try {
            val actions = MediaPlaybackState.ACTION_PLAY or
                    MediaPlaybackState.ACTION_PAUSE or
                    MediaPlaybackState.ACTION_PLAY_PAUSE or
                    MediaPlaybackState.ACTION_SKIP_TO_NEXT or
                    MediaPlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    MediaPlaybackState.ACTION_SEEK_TO or
                    MediaPlaybackState.ACTION_STOP

            val state = if (isPlaying) MediaPlaybackState.STATE_PLAYING else MediaPlaybackState.STATE_PAUSED

            val playbackStateBuilder = MediaPlaybackState.Builder()
                .setActions(actions)
                .setState(state, positionMs, 1.0f)

            mediaSession?.setPlaybackState(playbackStateBuilder.build())
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Error updating MediaSession state: ${e.message}")
        }
    }

    private fun updateMediaSessionMetadata(song: Song) {
        try {
            val metadataBuilder = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, song.title)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, song.artist)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, song.album)
                .putLong(MediaMetadata.METADATA_KEY_DURATION, song.durationMs)

            mediaSession?.setMetadata(metadataBuilder.build())
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Error updating MediaSession metadata: ${e.message}")
        }
    }

    private fun requestAudioFocus(): Boolean {
        if (hasAudioFocus) return true

        val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS -> {
                    Log.d("AudioPlayerEngine", "AudioFocus: Loss")
                    hasAudioFocus = false
                    resumeOnFocusGain = false
                    pause()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    Log.d("AudioPlayerEngine", "AudioFocus: Loss transient")
                    hasAudioFocus = false
                    if (_playbackState.value.isPlaying) {
                        resumeOnFocusGain = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    Log.d("AudioPlayerEngine", "AudioFocus: Duck")
                    setVolume(_playbackState.value.volume * 0.3f)
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    Log.d("AudioPlayerEngine", "AudioFocus: Gain")
                    hasAudioFocus = true
                    setVolume(1.0f)
                    if (resumeOnFocusGain) {
                        resumeOnFocusGain = false
                        resume()
                    }
                }
            }
        }

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(focusChangeListener)
                .build()

            audioFocusRequest = request
            audioManager.requestAudioFocus(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                focusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        hasAudioFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        return hasAudioFocus
    }

    private fun abandonAudioFocus() {
        if (!hasAudioFocus) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
        hasAudioFocus = false
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            try {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                context.registerReceiver(noisyReceiver, filter)
                isNoisyReceiverRegistered = true
            } catch (e: Exception) {
                Log.e("AudioPlayerEngine", "Could not register noisy receiver: ${e.message}")
            }
        }
    }

    private fun unregisterNoisyReceiver() {
        if (isNoisyReceiverRegistered) {
            try {
                context.unregisterReceiver(noisyReceiver)
                isNoisyReceiverRegistered = false
            } catch (_: Exception) {}
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        val newQueue = if (queue.isNotEmpty()) queue else listOf(song)
        val queueIndex = newQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        _playbackState.update {
            it.copy(
                currentSong = song,
                queue = newQueue,
                queueIndex = queueIndex,
                currentPositionMs = 0L,
                durationMs = if (song.durationMs > 0) song.durationMs else 180000L,
                isPlaying = true
            )
        }

        requestAudioFocus()
        registerNoisyReceiver()
        updateMediaSessionMetadata(song)
        updateMediaSessionState(true, 0L)

        // Start Foreground Service for background playback and notification controls
        MediaPlaybackService.startService(context)

        startPlayingSource(song)

        scope.launch(Dispatchers.IO) {
            songDao.incrementPlayCount(song.id, System.currentTimeMillis())
        }
    }

    private fun startPlayingSource(song: Song) {
        stopCurrentPlayback()

        if (song.pathOrUri.startsWith("content://") || song.pathOrUri.startsWith("file://") || song.pathOrUri.startsWith("http")) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, Uri.parse(song.pathOrUri))
                    setVolume(_playbackState.value.volume, _playbackState.value.volume)
                    prepareAsync()
                    setOnPreparedListener { mp ->
                        mp.start()
                        _playbackState.update {
                            it.copy(
                                isPlaying = true,
                                durationMs = mp.duration.toLong()
                            )
                        }
                        updateMediaSessionState(true, 0L)
                    }
                    setOnCompletionListener {
                        Log.d("AudioPlayerEngine", "Song completed naturally, advancing flow")
                        handlePlaybackCompletion()
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e("AudioPlayerEngine", "MediaPlayer error: what=$what, extra=$extra")
                        fallbackToSynthesizer(song)
                        true
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioPlayerEngine", "Could not play URI with MediaPlayer: ${e.message}, falling back to synthetic audio")
                fallbackToSynthesizer(song)
            }
        } else {
            fallbackToSynthesizer(song)
        }
    }

    private fun fallbackToSynthesizer(song: Song) {
        stopSyntheticAudio()
        try {
            val sampleRate = 44100
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            syntheticTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize.coerceAtLeast(8192))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            syntheticTrack?.play()
            syntheticTrack?.setVolume(_playbackState.value.volume)

            val baseFreq = when (song.id.hashCode() % 4) {
                0 -> 220.0 // A3 warm ambient chord
                1 -> 261.63 // C4 soft lofi
                2 -> 196.0 // G3 deep synth
                else -> 174.61 // F3 chill vibe
            }

            syntheticToneJob = scope.launch(Dispatchers.Default) {
                val buffer = ShortArray(2048)
                var phaseL = 0.0
                var phaseR = 0.0
                var modPhase = 0.0

                while (isActive && _playbackState.value.isPlaying) {
                    val volumeScale = 0.18f * _playbackState.value.volume
                    for (i in 0 until buffer.size step 2) {
                        modPhase += (2.0 * PI * 0.2) / sampleRate
                        val chorus = 1.0 + 0.03 * sin(modPhase)
                        val freqL = baseFreq * chorus
                        val freqR = (baseFreq * 1.5) * chorus

                        phaseL += (2.0 * PI * freqL) / sampleRate
                        phaseR += (2.0 * PI * freqR) / sampleRate

                        val sampleL = (sin(phaseL) * 0.7 + sin(phaseL * 0.5) * 0.3) * Short.MAX_VALUE * volumeScale
                        val sampleR = (sin(phaseR) * 0.6 + sin(phaseR * 2.0) * 0.2) * Short.MAX_VALUE * volumeScale

                        buffer[i] = sampleL.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                        buffer[i + 1] = sampleR.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    syntheticTrack?.write(buffer, 0, buffer.size)
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Synthetic tone generation error: ${e.message}")
        }
    }

    fun togglePlayPause() {
        val currentState = _playbackState.value
        if (currentState.currentSong == null) {
            if (currentState.queue.isNotEmpty()) {
                playSong(currentState.queue[0], currentState.queue)
            }
            return
        }

        if (currentState.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        _playbackState.update { it.copy(isPlaying = false) }
        try {
            mediaPlayer?.pause()
        } catch (_: Exception) {}
        stopSyntheticAudio()
        unregisterNoisyReceiver()
        updateMediaSessionState(false, _playbackState.value.currentPositionMs)
    }

    fun resume() {
        val song = _playbackState.value.currentSong ?: return
        requestAudioFocus()
        registerNoisyReceiver()
        _playbackState.update { it.copy(isPlaying = true) }
        MediaPlaybackService.startService(context)

        if (mediaPlayer != null) {
            try {
                mediaPlayer?.start()
            } catch (e: Exception) {
                startPlayingSource(song)
            }
        } else {
            fallbackToSynthesizer(song)
        }
        updateMediaSessionState(true, _playbackState.value.currentPositionMs)
    }

    fun seekTo(positionMs: Long) {
        val state = _playbackState.value
        val clamped = positionMs.coerceIn(0L, state.durationMs.coerceAtLeast(1000L))
        _playbackState.update { it.copy(currentPositionMs = clamped) }
        try {
            mediaPlayer?.seekTo(clamped.toInt())
        } catch (_: Exception) {}
        updateMediaSessionState(state.isPlaying, clamped)
    }

    fun playNext() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        val nextIndex = if (state.shuffleEnabled) {
            state.queue.indices.filter { it != state.queueIndex }.randomOrNull() ?: 0
        } else {
            (state.queueIndex + 1) % state.queue.size
        }

        val nextSong = state.queue.getOrNull(nextIndex) ?: return
        playSong(nextSong, state.queue)
    }

    fun playPrevious() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        if (state.currentPositionMs > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (state.queueIndex - 1 < 0) state.queue.size - 1 else state.queueIndex - 1
        val prevSong = state.queue.getOrNull(prevIndex) ?: return
        playSong(prevSong, state.queue)
    }

    fun toggleShuffle() {
        _playbackState.update { it.copy(shuffleEnabled = !it.shuffleEnabled) }
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.update { it.copy(repeatMode = nextMode) }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _playbackState.update { it.copy(volume = clamped) }
        try {
            mediaPlayer?.setVolume(clamped, clamped)
            syntheticTrack?.setVolume(clamped)
        } catch (_: Exception) {}
    }

    fun addToQueue(song: Song) {
        val updatedQueue = _playbackState.value.queue.toMutableList().apply { add(song) }
        _playbackState.update { it.copy(queue = updatedQueue) }
    }

    fun playNextInQueue(song: Song) {
        val state = _playbackState.value
        val updatedQueue = state.queue.toMutableList()
        val insertIndex = (state.queueIndex + 1).coerceAtMost(updatedQueue.size)
        updatedQueue.add(insertIndex, song)
        _playbackState.update { it.copy(queue = updatedQueue) }
    }

    fun addSongsToQueue(songs: List<Song>) {
        if (songs.isEmpty()) return
        val state = _playbackState.value
        val updatedQueue = state.queue.toMutableList().apply { addAll(songs) }
        _playbackState.update { it.copy(queue = updatedQueue) }
    }

    fun updateSongFavoriteStatus(songId: String, isFavorite: Boolean) {
        _playbackState.update { state ->
            val updatedCurrent = if (state.currentSong?.id == songId) {
                state.currentSong.copy(isFavorite = isFavorite)
            } else {
                state.currentSong
            }
            val updatedQueue = state.queue.map { s ->
                if (s.id == songId) s.copy(isFavorite = isFavorite) else s
            }
            state.copy(
                currentSong = updatedCurrent,
                queue = updatedQueue
            )
        }
    }

    fun removeFromQueue(index: Int) {
        val state = _playbackState.value
        if (index in state.queue.indices) {
            val updatedQueue = state.queue.toMutableList().apply { removeAt(index) }
            val newIndex = if (index < state.queueIndex) state.queueIndex - 1 else state.queueIndex
            _playbackState.update { it.copy(queue = updatedQueue, queueIndex = newIndex.coerceAtLeast(0)) }
        }
    }

    fun clearQueue() {
        val current = _playbackState.value.currentSong
        _playbackState.update {
            it.copy(
                queue = if (current != null) listOf(current) else emptyList(),
                queueIndex = 0
            )
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val state = _playbackState.value
        if (fromIndex in state.queue.indices && toIndex in state.queue.indices) {
            val updated = state.queue.toMutableList()
            val item = updated.removeAt(fromIndex)
            updated.add(toIndex, item)
            val newCurrentIndex = updated.indexOfFirst { it.id == state.currentSong?.id }.coerceAtLeast(0)
            _playbackState.update { it.copy(queue = updated, queueIndex = newCurrentIndex) }
        }
    }

    private fun handlePlaybackCompletion() {
        val state = _playbackState.value
        when (state.repeatMode) {
            RepeatMode.ONE -> {
                seekTo(0L)
                resume()
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                if (state.queueIndex < state.queue.size - 1) {
                    playNext()
                } else {
                    pause()
                    seekTo(0L)
                }
            }
        }
    }

    private fun startProgressAndWaveformUpdater() {
        progressPollingJob?.cancel()
        progressPollingJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (isActive) {
                delay(120)
                step++
                val state = _playbackState.value
                if (state.isPlaying && state.currentSong != null) {
                    val currentPos = if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.currentPosition?.toLong() ?: (state.currentPositionMs + 120)
                    } else {
                        state.currentPositionMs + 120
                    }

                    if (currentPos >= state.durationMs && state.durationMs > 0) {
                        launch(Dispatchers.Main) { handlePlaybackCompletion() }
                    } else {
                        val amplitudes = generateDynamicWaveform(step, state.progress)
                        _playbackState.update {
                            it.copy(
                                currentPositionMs = currentPos,
                                waveformAmplitudes = amplitudes
                            )
                        }
                    }
                } else if (!state.isPlaying && state.waveformAmplitudes.isEmpty()) {
                    val idleAmplitudes = (0 until 32).map { 0.25f }
                    _playbackState.update { it.copy(waveformAmplitudes = idleAmplitudes) }
                }
            }
        }
    }

    private fun generateDynamicWaveform(step: Int, progress: Float): List<Float> {
        val count = 36
        return (0 until count).map { i ->
            val phase = step * 0.15f + i * 0.35f
            val base = (sin(phase) * 0.5f + 0.5f)
            val subHarmonic = (sin(phase * 0.5f + 1.2f) * 0.3f)
            val envelope = (sin((i.toFloat() / count.toFloat()) * PI.toFloat()) * 0.8f + 0.2f)
            ((base * 0.7f + subHarmonic + 0.15f) * envelope).coerceIn(0.12f, 1.0f)
        }
    }

    private fun stopSyntheticAudio() {
        syntheticToneJob?.cancel()
        syntheticToneJob = null
        try {
            syntheticTrack?.pause()
            syntheticTrack?.flush()
            syntheticTrack?.stop()
            syntheticTrack?.release()
        } catch (_: Exception) {}
        syntheticTrack = null
    }

    private fun stopCurrentPlayback() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                mp.release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        stopSyntheticAudio()
    }

    fun release() {
        stopCurrentPlayback()
        progressPollingJob?.cancel()
        unregisterNoisyReceiver()
        abandonAudioFocus()
        try {
            mediaSession?.isActive = false
            mediaSession?.release()
            mediaSession = null
        } catch (_: Exception) {}
        MediaPlaybackService.stopService(context)
    }
}
