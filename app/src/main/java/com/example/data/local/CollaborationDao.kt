package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CollaborationActivity
import com.example.data.model.Collaborator
import com.example.data.model.SyncQueueItem
import com.example.data.model.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface CollaborationDao {
    @Query("SELECT * FROM collaborators WHERE playlistId = :playlistId ORDER BY role DESC, name ASC")
    fun getCollaborators(playlistId: String): Flow<List<Collaborator>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborators(collaborators: List<Collaborator>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollaborator(collaborator: Collaborator)

    @Query("DELETE FROM collaborators WHERE playlistId = :playlistId AND userId = :userId")
    suspend fun removeCollaborator(playlistId: String, userId: String)

    @Query("SELECT * FROM collaboration_activities WHERE playlistId = :playlistId ORDER BY timestamp DESC LIMIT 30")
    fun getActivitiesForPlaylist(playlistId: String): Flow<List<CollaborationActivity>>

    @Query("SELECT * FROM collaboration_activities ORDER BY timestamp DESC LIMIT 30")
    fun getAllRecentActivities(): Flow<List<CollaborationActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: CollaborationActivity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<CollaborationActivity>)

    // Sync Queue
    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY timestamp ASC")
    fun getPendingSyncItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE isSynced = 0")
    fun getPendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSyncItem(item: SyncQueueItem)

    @Query("UPDATE sync_queue SET isSynced = 1 WHERE id = :id")
    suspend fun markItemSynced(id: Long)

    @Query("DELETE FROM sync_queue WHERE isSynced = 1")
    suspend fun clearSyncedItems()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettings(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getUserSettingsSync(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)

    @Query("UPDATE user_settings SET isOnlineSyncEnabled = :enabled WHERE id = 1")
    suspend fun setOnlineSyncEnabled(enabled: Boolean)

    @Query("UPDATE user_settings SET userName = :name WHERE id = 1")
    suspend fun updateUserName(name: String)

    @Query("UPDATE user_settings SET equalizerPreset = :preset WHERE id = 1")
    suspend fun updateEqualizerPreset(preset: String)

    @Query("UPDATE user_settings SET sleepTimerMinutes = :minutes WHERE id = 1")
    suspend fun updateSleepTimer(minutes: Int)

    @Query("UPDATE user_settings SET themeMode = :mode WHERE id = 1")
    suspend fun updateThemeMode(mode: String)
}
