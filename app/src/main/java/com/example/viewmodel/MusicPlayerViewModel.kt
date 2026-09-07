package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CollaborationActivity
import com.example.data.model.Collaborator
import com.example.data.model.PlaybackState
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.UserSettings
import com.example.data.repository.CollaborationRepository
import com.example.data.repository.MusicRepository
import com.example.data.repository.SettingsRepository
import com.example.player.AudioPlayerEngine
import com.example.scanner.MediaScanner
import com.example.scanner.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val playerEngine = AudioPlayerEngine.getInstance(application)
    private val musicRepo = MusicRepository(application)
    private val collabRepo = CollaborationRepository(application)
    private val settingsRepo = SettingsRepository(application)
    private val mediaScanner = MediaScanner(application)

    val playbackState: StateFlow<PlaybackState> = playerEngine.playbackState

    val allSongs: StateFlow<List<Song>> = musicRepo.allSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteSongs: StateFlow<List<Song>> = musicRepo.favoriteSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyPlayedSongs: StateFlow<List<Song>> = musicRepo.recentlyPlayedSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyAddedSongs: StateFlow<List<Song>> = musicRepo.recentlyAddedSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val mostPlayedSongs: StateFlow<List<Song>> = musicRepo.mostPlayedSongs
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalSongCount: StateFlow<Int> = musicRepo.totalSongCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val allPlaylists: StateFlow<List<Playlist>> = musicRepo.allPlaylists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val personalPlaylists: StateFlow<List<Playlist>> = musicRepo.personalPlaylists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val collaborativePlaylists: StateFlow<List<Playlist>> = musicRepo.collaborativePlaylists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val albums: StateFlow<List<String>> = musicRepo.albums
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val artists: StateFlow<List<String>> = musicRepo.artists
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val genres: StateFlow<List<String>> = musicRepo.genres
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val folders: StateFlow<List<String>> = musicRepo.folders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val userSettings: StateFlow<UserSettings?> = settingsRepo.userSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val pendingSyncCount: StateFlow<Int> = collabRepo.pendingSyncCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val recentActivities: StateFlow<List<CollaborationActivity>> = collabRepo.allRecentActivities
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // UI View State
    private val _isFullPlayerExpanded = MutableStateFlow(false)
    val isFullPlayerExpanded: StateFlow<Boolean> = _isFullPlayerExpanded.asStateFlow()

    private val _isQueueSheetVisible = MutableStateFlow(false)
    val isQueueSheetVisible: StateFlow<Boolean> = _isQueueSheetVisible.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    private val _selectedSongForPlaylist = MutableStateFlow<Song?>(null)
    val selectedSongForPlaylist: StateFlow<Song?> = _selectedSongForPlaylist.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen: StateFlow<Boolean> = _isCreatePlaylistDialogOpen.asStateFlow()

    private val _isJoinCollabDialogOpen = MutableStateFlow(false)
    val isJoinCollabDialogOpen: StateFlow<Boolean> = _isJoinCollabDialogOpen.asStateFlow()

    private val _isSettingsDialogOpen = MutableStateFlow(false)
    val isSettingsDialogOpen: StateFlow<Boolean> = _isSettingsDialogOpen.asStateFlow()

    init {
        viewModelScope.launch {
            mediaScanner.initializeDatabaseIfNeeded()
        }
    }

    // Playback actions
    fun playSong(song: Song, queue: List<Song> = emptyList(), targetIndex: Int? = null) {
        playerEngine.playSong(song, queue, targetIndex)
    }

    fun setShuffleEnabled(enabled: Boolean) {
        playerEngine.setShuffleEnabled(enabled)
    }

    fun togglePlayPause() {
        playerEngine.togglePlayPause()
    }

    fun playNext() {
        playerEngine.playNext()
    }

    fun playPrevious() {
        playerEngine.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerEngine.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerEngine.toggleShuffle()
    }

    fun toggleRepeatMode() {
        playerEngine.toggleRepeatMode()
    }

    fun setVolume(vol: Float) {
        playerEngine.setVolume(vol)
    }

    fun addToQueue(song: Song) {
        playerEngine.addToQueue(song)
    }

    fun removeFromQueue(index: Int) {
        playerEngine.removeFromQueue(index)
    }

    fun clearQueue() {
        playerEngine.clearQueue()
    }

    fun playNextInQueue(song: Song) {
        playerEngine.playNextInQueue(song)
    }

    fun addAlbumToQueue(songs: List<Song>) {
        playerEngine.addSongsToQueue(songs)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        playerEngine.reorderQueue(fromIndex, toIndex)
    }

    // Favorite actions - keeps song playing uninterrupted
    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val newFavStatus = !song.isFavorite
            musicRepo.toggleSongFavorite(song.id, newFavStatus)
            // Seamless in-place update without restarting playback or seeking
            playerEngine.updateSongFavoriteStatus(song.id, newFavStatus)
        }
    }

    fun toggleFavoritePlaylist(playlistId: String, currentIsFav: Boolean) {
        viewModelScope.launch {
            musicRepo.toggleFavoritePlaylist(playlistId, !currentIsFav)
        }
    }

    // Playlist actions
    fun createPlaylist(name: String, description: String, isCollab: Boolean = false) {
        viewModelScope.launch {
            if (isCollab) {
                collabRepo.createCollaborativePlaylist(
                    name = name,
                    description = description,
                    ownerName = userSettings.value?.userName ?: "Alex Rivera"
                )
            } else {
                musicRepo.createPlaylist(name, description)
            }
            _isCreatePlaylistDialogOpen.value = false
        }
    }

    fun joinCollabPlaylist(inviteCode: String) {
        viewModelScope.launch {
            val user = userSettings.value?.userName ?: "Alex Rivera"
            collabRepo.joinByInviteCode(inviteCode, user)
            _isJoinCollabDialogOpen.value = false
        }
    }

    fun addSongToPlaylist(playlistId: String, songId: String, isCollab: Boolean = false, songTitle: String = "") {
        viewModelScope.launch {
            val user = userSettings.value?.userName ?: "Alex Rivera"
            if (isCollab) {
                collabRepo.addSongToCollabPlaylist(playlistId, songId, songTitle, user)
            } else {
                musicRepo.addSongToPlaylist(playlistId, songId, user)
            }
            _selectedSongForPlaylist.value = null
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            musicRepo.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            musicRepo.deletePlaylist(playlistId)
        }
    }

    fun simulateCollabActivity(playlistId: String) {
        viewModelScope.launch {
            collabRepo.simulateFriendActivity(playlistId)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            collabRepo.syncPendingChanges()
            _scanMessage.value = "All playlists & collaborative changes synchronized."
        }
    }

    fun scanDeviceMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            val result = mediaScanner.scanDeviceAudioFiles()
            _isScanning.value = false
            _scanMessage.value = result.message
        }
    }

    fun clearScanMessage() {
        _scanMessage.value = null
    }

    fun updateOnlineSyncMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateOnlineSync(enabled)
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            settingsRepo.updateUserName(name)
        }
    }

    fun updateEqualizer(preset: String) {
        viewModelScope.launch {
            settingsRepo.updateEqualizer(preset)
        }
    }

    fun updateSleepTimer(minutes: Int) {
        viewModelScope.launch {
            settingsRepo.updateSleepTimer(minutes)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFullPlayerExpanded(expanded: Boolean) {
        _isFullPlayerExpanded.value = expanded
    }

    fun setQueueSheetVisible(visible: Boolean) {
        _isQueueSheetVisible.value = visible
    }

    fun setSelectedSongForPlaylist(song: Song?) {
        _selectedSongForPlaylist.value = song
    }

    fun setCreatePlaylistDialogOpen(open: Boolean) {
        _isCreatePlaylistDialogOpen.value = open
    }

    fun setJoinCollabDialogOpen(open: Boolean) {
        _isJoinCollabDialogOpen.value = open
    }

    fun setSettingsDialogOpen(open: Boolean) {
        _isSettingsDialogOpen.value = open
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepo.updateThemeMode(mode)
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = userSettings.value?.themeMode ?: "DARK"
            val next = if (current == "DARK") "LIGHT" else "DARK"
            settingsRepo.updateThemeMode(next)
        }
    }

    fun getSongsForPlaylist(playlistId: String) = musicRepo.getSongsForPlaylist(playlistId)
    fun getPlaylistById(playlistId: String) = musicRepo.getPlaylistById(playlistId)
    fun getCollaborators(playlistId: String) = collabRepo.getCollaborators(playlistId)
    fun getActivities(playlistId: String) = collabRepo.getActivities(playlistId)
    fun getSongsByAlbum(album: String) = musicRepo.getSongsByAlbum(album)
    fun getSongsByArtist(artist: String) = musicRepo.getSongsByArtist(artist)
    fun getSongsByGenre(genre: String) = musicRepo.getSongsByGenre(genre)
    fun getSongsByFolder(folder: String) = musicRepo.getSongsByFolder(folder)

    override fun onCleared() {
        super.onCleared()
        playerEngine.release()
    }
}
