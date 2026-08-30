package com.example.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.WavyxDatabase
import com.example.data.model.Song
import com.example.data.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context) {

    private val database = WavyxDatabase.getDatabase(context)
    private val songDao = database.songDao()
    private val playlistDao = database.playlistDao()
    private val collaborationDao = database.collaborationDao()
    private val userSettingsDao = database.userSettingsDao()

    suspend fun initializeDatabaseIfNeeded() = withContext(Dispatchers.IO) {
        val existingSettings = userSettingsDao.getUserSettingsSync()
        if (existingSettings == null) {
            userSettingsDao.insertUserSettings(UserSettings())
        }
        // Always purge any demo songs / playlists so the library is purely the user's local files
        songDao.purgeDemoSongs()
        playlistDao.purgeDemoPlaylists()
        playlistDao.purgeDemoPlaylistCrossRefs()

        // Automatically scan device storage for local tracks
        scanDeviceAudioFiles()
    }

    suspend fun scanDeviceAudioFiles(): ScanResult = withContext(Dispatchers.IO) {
        val scannedSongs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val albumIdColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val trackColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
                val dateAddedColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    val album = cursor.getString(albumColumn) ?: "Unknown Album"
                    val duration = cursor.getLong(durationColumn)
                    val dataPath = if (dataColumn != -1) cursor.getString(dataColumn) else ""
                    val albumId = if (albumIdColumn != -1) cursor.getLong(albumIdColumn) else -1L
                    val track = if (trackColumn != -1) cursor.getInt(trackColumn) else 1
                    val dateAdded = if (dateAddedColumn != -1) cursor.getLong(dateAddedColumn) * 1000L else System.currentTimeMillis()

                    val contentUri = ContentUris.withAppendedId(collection, id)
                    val albumArtUri = if (albumId != -1L) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    val folderName = if (dataPath.isNotBlank()) {
                        try {
                            val parent = File(dataPath).parentFile?.name
                            parent ?: "Music"
                        } catch (e: Exception) {
                            "Music"
                        }
                    } else "Internal Storage"

                    val song = Song(
                        id = "local_media_$id",
                        title = title,
                        artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                        album = if (album == "<unknown>") "Unknown Album" else album,
                        genre = "Local Audio",
                        durationMs = duration,
                        pathOrUri = contentUri.toString(),
                        albumArtUri = albumArtUri,
                        trackNumber = track,
                        isFavorite = false,
                        playCount = 0,
                        lastPlayedTimestamp = 0L,
                        dateAddedTimestamp = dateAdded,
                        isLocalFile = true,
                        folderName = folderName
                    )
                    scannedSongs.add(song)
                }
            }

            if (scannedSongs.isNotEmpty()) {
                songDao.insertSongs(scannedSongs)
            }
            ScanResult(success = true, songsFound = scannedSongs.size, message = "Indexed ${scannedSongs.size} local tracks.")
        } catch (e: Exception) {
            ScanResult(success = false, songsFound = 0, message = "Scanning encountered an issue: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    suspend fun cleanMissingFiles(): Int = withContext(Dispatchers.IO) {
        // checks local files that may no longer exist
        var removedCount = 0
        // Clean missing files logic
        removedCount
    }
}

data class ScanResult(
    val success: Boolean,
    val songsFound: Int,
    val message: String
)
