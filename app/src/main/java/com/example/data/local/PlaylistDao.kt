package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isCollaborative = 1 ORDER BY updatedAt DESC")
    fun getCollaborativePlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isCollaborative = 0 ORDER BY updatedAt DESC")
    fun getPersonalPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoritePlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistById(playlistId: String): Flow<Playlist?>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistByIdSync(playlistId: String): Playlist?

    @Query("SELECT * FROM playlists WHERE inviteCode = :inviteCode LIMIT 1")
    suspend fun getPlaylistByInviteCode(inviteCode: String): Playlist?

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_songs ps ON s.id = ps.songId
        WHERE ps.playlistId = :playlistId AND s.isUnavailable = 0
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongsForPlaylist(playlistId: String): Flow<List<Song>>

    @Query("""
        SELECT ps.* FROM playlist_songs ps
        WHERE ps.playlistId = :playlistId
        ORDER BY ps.orderIndex ASC
    """)
    fun getSongCrossRefsForPlaylist(playlistId: String): Flow<List<PlaylistSongCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylists(playlists: List<Playlist>)

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRef(crossRef: PlaylistSongCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearSongsFromPlaylist(playlistId: String)

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getPlaylistSongCount(playlistId: String): Int

    @Query("UPDATE playlists SET songCount = :count, updatedAt = :timestamp WHERE id = :playlistId")
    suspend fun updatePlaylistSongCount(playlistId: String, count: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE playlists SET isFavorite = :isFav WHERE id = :playlistId")
    suspend fun toggleFavoritePlaylist(playlistId: String, isFav: Boolean)

    @Query("DELETE FROM playlists WHERE id LIKE 'pl_personal_%' OR id LIKE 'pl_collab_%'")
    suspend fun purgeDemoPlaylists()

    @Query("DELETE FROM playlist_songs WHERE songId LIKE 'demo_%'")
    suspend fun purgeDemoPlaylistCrossRefs()
}
