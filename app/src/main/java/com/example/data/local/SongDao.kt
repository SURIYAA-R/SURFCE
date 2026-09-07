package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs WHERE isUnavailable = 0 ORDER BY title ASC")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 AND isUnavailable = 0 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 AND isUnavailable = 0 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    fun getRecentlyPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestamp > 0 AND isUnavailable = 0 ORDER BY lastPlayedTimestamp DESC LIMIT 20")
    suspend fun getRecentlyPlayedSongsSync(): List<Song>

    @Query("SELECT * FROM songs WHERE isUnavailable = 0 ORDER BY title ASC")
    suspend fun getAllSongsSync(): List<Song>

    @Query("SELECT * FROM songs WHERE isUnavailable = 0 ORDER BY dateAddedTimestamp DESC LIMIT 20")
    fun getRecentlyAddedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE playCount > 0 AND isUnavailable = 0 ORDER BY playCount DESC LIMIT 20")
    fun getMostPlayedSongs(): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): Song?

    @Query("SELECT * FROM songs WHERE album = :albumName AND isUnavailable = 0 ORDER BY trackNumber ASC, title ASC")
    fun getSongsByAlbum(albumName: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE artist = :artistName AND isUnavailable = 0 ORDER BY title ASC")
    fun getSongsByArtist(artistName: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE genre = :genreName AND isUnavailable = 0 ORDER BY title ASC")
    fun getSongsByGenre(genreName: String): Flow<List<Song>>

    @Query("SELECT * FROM songs WHERE folderName = :folderName AND isUnavailable = 0 ORDER BY title ASC")
    fun getSongsByFolder(folderName: String): Flow<List<Song>>

    @Query("""
        SELECT * FROM songs 
        WHERE isUnavailable = 0 AND (
            title LIKE '%' || :query || '%' OR 
            artist LIKE '%' || :query || '%' OR 
            album LIKE '%' || :query || '%' OR 
            genre LIKE '%' || :query || '%' OR
            folderName LIKE '%' || :query || '%'
        )
        ORDER BY title ASC
    """)
    fun searchSongs(query: String): Flow<List<Song>>

    @Query("SELECT DISTINCT album FROM songs WHERE isUnavailable = 0 ORDER BY album ASC")
    fun getAllAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM songs WHERE isUnavailable = 0 ORDER BY artist ASC")
    fun getAllArtists(): Flow<List<String>>

    @Query("SELECT DISTINCT genre FROM songs WHERE isUnavailable = 0 ORDER BY genre ASC")
    fun getAllGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT folderName FROM songs WHERE isUnavailable = 0 ORDER BY folderName ASC")
    fun getAllFolders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSong(song: Song)

    @Query("SELECT COUNT(*) FROM songs WHERE isUnavailable = 0")
    suspend fun getSongCountSync(): Int

    @Update
    suspend fun updateSong(song: Song)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: String, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestamp = :timestamp WHERE id = :songId")
    suspend fun incrementPlayCount(songId: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE songs SET isUnavailable = 1 WHERE id = :songId")
    suspend fun markUnavailable(songId: String)

    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: String)

    @Query("DELETE FROM songs WHERE isLocalFile = 0 OR id LIKE 'demo_%'")
    suspend fun purgeDemoSongs()

    @Query("SELECT COUNT(*) FROM songs WHERE isUnavailable = 0")
    fun getSongCount(): Flow<Int>
}
