package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collaborators")
data class Collaborator(
    @PrimaryKey
    val id: String, // composite or unique ID: "${playlistId}_${userId}"
    val playlistId: String,
    val userId: String,
    val name: String,
    val avatarColorHex: String = "#38BDF8",
    val isOnline: Boolean = true,
    val role: String = "Editor", // Owner, Editor, Viewer
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "collaboration_activities")
data class CollaborationActivity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: String,
    val userName: String,
    val actionText: String, // e.g. "Arun added Blinding Lights"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String, // CREATE_PLAYLIST, ADD_SONG, REMOVE_SONG, UPDATE_COLLABORATION
    val entityId: String,
    val payloadJson: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val userName: String = "Alex Rivera",
    val email: String = "alex.wavyx@music.io",
    val isOnlineSyncEnabled: Boolean = true,
    val audioQuality: String = "Lossless (FLAC/320kbps)",
    val equalizerPreset: String = "Deep Bass & Warm Waves",
    val sleepTimerMinutes: Int = 0, // 0 = off
    val gaplessPlayback: Boolean = true,
    val crossfadeSeconds: Int = 2,
    val autoScanOnStartup: Boolean = true,
    val themeMode: String = "DARK" // "DARK", "LIGHT", "SYSTEM"
)
