package com.example.data.repository

import android.content.Context
import com.example.data.local.WavyxDatabase
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class MusicRepository(context: Context) {
    private val database = WavyxDatabase.getDatabase(context)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()

    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongs()
    val recentlyPlayedSongs: Flow<List<Song>> = songDao.getRecentlyPlayedSongs()
    val recentlyAddedSongs: Flow<List<Song>> = songDao.getRecentlyAddedSongs()
    val mostPlayedSongs: Flow<List<Song>> = songDao.getMostPlayedSongs()
    val totalSongCount: Flow<Int> = songDao.getSongCount()

    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val personalPlaylists: Flow<List<Playlist>> = playlistDao.getPersonalPlaylists()
    val collaborativePlaylists: Flow<List<Playlist>> = playlistDao.getCollaborativePlaylists()
    val favoritePlaylists: Flow<List<Playlist>> = playlistDao.getFavoritePlaylists()

    val albums: Flow<List<String>> = songDao.getAllAlbums()
    val artists: Flow<List<String>> = songDao.getAllArtists()
    val genres: Flow<List<String>> = songDao.getAllGenres()
    val folders: Flow<List<String>> = songDao.getAllFolders()

    fun getSongsByAlbum(album: String): Flow<List<Song>> = songDao.getSongsByAlbum(album)
    fun getSongsByArtist(artist: String): Flow<List<Song>> = songDao.getSongsByArtist(artist)
    fun getSongsByGenre(genre: String): Flow<List<Song>> = songDao.getSongsByGenre(genre)
    fun getSongsByFolder(folder: String): Flow<List<Song>> = songDao.getSongsByFolder(folder)
    fun searchSongs(query: String): Flow<List<Song>> = songDao.searchSongs(query)

    fun getPlaylistById(playlistId: String): Flow<Playlist?> = playlistDao.getPlaylistById(playlistId)
    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>> = playlistDao.getSongsForPlaylist(playlistId)

    suspend fun toggleSongFavorite(songId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        songDao.updateFavorite(songId, isFavorite)
    }

    suspend fun createPlaylist(name: String, description: String = "", coverUri: String? = null): Playlist = withContext(Dispatchers.IO) {
        val newPlaylist = Playlist(
            id = "pl_${UUID.randomUUID().toString().take(8)}",
            name = name,
            description = description,
            coverUri = coverUri ?: "drawable://img_cover_midnight_waves",
            isCollaborative = false,
            ownerName = "You",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            songCount = 0
        )
        playlistDao.insertPlaylist(newPlaylist)
        newPlaylist
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String, addedBy: String = "You") = withContext(Dispatchers.IO) {
        val currentCount = playlistDao.getPlaylistSongCount(playlistId)
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            orderIndex = currentCount,
            addedBy = addedBy,
            addedAtTimestamp = System.currentTimeMillis()
        )
        playlistDao.insertPlaylistSongCrossRef(crossRef)
        playlistDao.updatePlaylistSongCount(playlistId, currentCount + 1)
    }

    suspend fun removeSongFromPlaylist(playlistId: String, songId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        val newCount = playlistDao.getPlaylistSongCount(playlistId)
        playlistDao.updatePlaylistSongCount(playlistId, newCount)
    }

    suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun toggleFavoritePlaylist(playlistId: String, isFav: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.toggleFavoritePlaylist(playlistId, isFav)
    }

    suspend fun markUnavailable(songId: String) = withContext(Dispatchers.IO) {
        songDao.markUnavailable(songId)
    }

    suspend fun deleteSong(songId: String) = withContext(Dispatchers.IO) {
        songDao.deleteSong(songId)
    }
}
