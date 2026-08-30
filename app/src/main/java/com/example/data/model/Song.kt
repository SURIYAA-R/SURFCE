package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey
    val id: String, // String ID (e.g. URI hash, media store ID or custom ID)
    val title: String,
    val artist: String,
    val album: String,
    val genre: String = "Unknown",
    val durationMs: Long = 0L,
    val pathOrUri: String,
    val albumArtUri: String? = null,
    val trackNumber: Int = 1,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestamp: Long = 0L,
    val dateAddedTimestamp: Long = System.currentTimeMillis(),
    val isLocalFile: Boolean = true,
    val isUnavailable: Boolean = false,
    val folderName: String = "Internal Storage"
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}
