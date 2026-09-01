package com.example.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MediaPlaybackService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var playbackObserverJob: Job? = null
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val CHANNEL_ID = "surfce_music_channel"
        const val CHANNEL_NAME = "SURFCE Music Playback"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.surfce.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.surfce.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.surfce.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.surfce.ACTION_NEXT"
        const val ACTION_PREV = "com.example.surfce.ACTION_PREV"
        const val ACTION_STOP = "com.example.surfce.ACTION_STOP"

        fun startService(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("MediaPlaybackService", "Failed to start service: ${e.message}")
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("MediaPlaybackService", "Failed to stop service: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        observePlaybackState()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val engine = AudioPlayerEngine.getInstance(applicationContext)

        when (intent?.action) {
            ACTION_PLAY -> engine.resume()
            ACTION_PAUSE -> engine.pause()
            ACTION_TOGGLE_PLAY_PAUSE -> engine.togglePlayPause()
            ACTION_NEXT -> engine.playNext()
            ACTION_PREV -> engine.playPrevious()
            ACTION_STOP -> {
                engine.pause()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        // Display initial notification so foreground service is active immediately
        val currentState = engine.playbackState.value
        if (currentState.currentSong != null) {
            updateNotification(currentState.currentSong, currentState.isPlaying)
        }

        return START_STICKY
    }

    private fun observePlaybackState() {
        playbackObserverJob?.cancel()
        playbackObserverJob = scope.launch {
            val engine = AudioPlayerEngine.getInstance(applicationContext)
            engine.playbackState.collectLatest { state ->
                val song = state.currentSong
                if (song != null) {
                    updateNotification(song, state.isPlaying)
                } else {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            }
        }
    }

    private fun updateNotification(song: Song, isPlaying: Boolean) {
        val notification = buildMediaNotification(song, isPlaying)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.e("MediaPlaybackService", "Error calling startForeground: ${e.message}")
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun buildMediaNotification(song: Song, isPlaying: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevPendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseActionIntent = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPausePendingIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, MediaPlaybackService::class.java).apply { action = playPauseActionIntent },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextPendingIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopPendingIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, MediaPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val engine = AudioPlayerEngine.getInstance(applicationContext)
        val sessionToken = engine.getMediaSessionToken()

        val largeCoverBitmap: Bitmap? = try {
            val coverRes = when {
                song.album.contains("Velvet") -> R.drawable.img_cover_neon_echoes
                song.album.contains("Resonant") -> R.drawable.img_cover_deep_focus
                else -> R.drawable.img_cover_midnight_waves
            }
            BitmapFactory.decodeResource(resources, coverRes)
        } catch (_: Exception) {
            null
        }

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle(song.title)
            .setContentText("${song.artist} • ${song.album}")
            .setSubText("SURFCE Audio")
            .setContentIntent(contentPendingIntent)
            .setDeleteIntent(stopPendingIntent)
            .setOngoing(isPlaying)
            .setVisibility(Notification.VISIBILITY_PUBLIC)

        if (largeCoverBitmap != null) {
            builder.setLargeIcon(largeCoverBitmap)
        }

        // Add Previous Action
        val prevIcon = Icon.createWithResource(this, R.drawable.ic_prev)
        val prevAction = Notification.Action.Builder(prevIcon, "Previous", prevPendingIntent).build()
        builder.addAction(prevAction)

        // Add Play / Pause Action
        val playPauseIconRes = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        val playPauseTitle = if (isPlaying) "Pause" else "Play"
        val playPauseIcon = Icon.createWithResource(this, playPauseIconRes)
        val playPauseAction = Notification.Action.Builder(playPauseIcon, playPauseTitle, playPausePendingIntent).build()
        builder.addAction(playPauseAction)

        // Add Next Action
        val nextIcon = Icon.createWithResource(this, R.drawable.ic_next)
        val nextAction = Notification.Action.Builder(nextIcon, "Next", nextPendingIntent).build()
        builder.addAction(nextAction)

        // Add MediaStyle with MediaSession token and compact actions
        val mediaStyle = Notification.MediaStyle()
            .setShowActionsInCompactView(0, 1, 2)

        if (sessionToken != null) {
            mediaStyle.setMediaSession(sessionToken)
        }
        builder.setStyle(mediaStyle)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Playback controls and track information for background music."
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackObserverJob?.cancel()
    }
}
