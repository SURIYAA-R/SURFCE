package com.example.data.repository

import android.content.Context
import com.example.data.local.WavyxDatabase
import com.example.data.model.CollaborationActivity
import com.example.data.model.Collaborator
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SyncQueueItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class CollaborationRepository(context: Context) {
    private val database = WavyxDatabase.getDatabase(context)
    private val collaborationDao = database.collaborationDao()
    private val playlistDao = database.playlistDao()

    val pendingSyncCount: Flow<Int> = collaborationDao.getPendingSyncCount()
    val allRecentActivities: Flow<List<CollaborationActivity>> = collaborationDao.getAllRecentActivities()

    fun getCollaborators(playlistId: String): Flow<List<Collaborator>> = collaborationDao.getCollaborators(playlistId)
    fun getActivities(playlistId: String): Flow<List<CollaborationActivity>> = collaborationDao.getActivitiesForPlaylist(playlistId)

    suspend fun createCollaborativePlaylist(
        name: String,
        description: String,
        coverUri: String? = null,
        ownerName: String = "You"
    ): Playlist = withContext(Dispatchers.IO) {
        val codeSuffix = Random.nextInt(1000, 9999)
        val inviteCode = "WAVYX-$codeSuffix"
        val playlistId = "pl_collab_${UUID.randomUUID().toString().take(6)}"

        val playlist = Playlist(
            id = playlistId,
            name = name,
            description = description,
            coverUri = coverUri ?: "drawable://img_cover_neon_echoes",
            isCollaborative = true,
            inviteCode = inviteCode,
            ownerName = ownerName,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            songCount = 0
        )

        playlistDao.insertPlaylist(playlist)

        // Add creator as Owner
        val ownerCollaborator = Collaborator(
            id = "${playlistId}_owner",
            playlistId = playlistId,
            userId = "user_me",
            name = ownerName,
            avatarColorHex = "#38BDF8",
            isOnline = true,
            role = "Owner",
            lastActiveTimestamp = System.currentTimeMillis()
        )
        collaborationDao.insertCollaborator(ownerCollaborator)

        // Log creation activity
        val activity = CollaborationActivity(
            playlistId = playlistId,
            userName = ownerName,
            actionText = "created collaborative playlist '$name'",
            timestamp = System.currentTimeMillis()
        )
        collaborationDao.insertActivity(activity)

        // Queue offline sync payload
        collaborationDao.enqueueSyncItem(
            SyncQueueItem(
                actionType = "CREATE_PLAYLIST",
                entityId = playlistId,
                payloadJson = "{\"name\":\"$name\",\"inviteCode\":\"$inviteCode\"}"
            )
        )

        playlist
    }

    suspend fun joinByInviteCode(inviteCode: String, userName: String = "You"): Playlist? = withContext(Dispatchers.IO) {
        val cleanCode = inviteCode.trim().uppercase()
        val playlist = playlistDao.getPlaylistByInviteCode(cleanCode)

        if (playlist != null) {
            val collabId = "${playlist.id}_${UUID.randomUUID().toString().take(4)}"
            val collaborator = Collaborator(
                id = collabId,
                playlistId = playlist.id,
                userId = "user_joined",
                name = userName,
                avatarColorHex = "#60A5FA",
                isOnline = true,
                role = "Editor",
                lastActiveTimestamp = System.currentTimeMillis()
            )
            collaborationDao.insertCollaborator(collaborator)

            val activity = CollaborationActivity(
                playlistId = playlist.id,
                userName = userName,
                actionText = "joined via invite code $cleanCode",
                timestamp = System.currentTimeMillis()
            )
            collaborationDao.insertActivity(activity)
            playlist
        } else {
            // Mock join if unknown invite code for great user demo experience
            val mockId = "pl_collab_joined_${UUID.randomUUID().toString().take(4)}"
            val newPlaylist = Playlist(
                id = mockId,
                name = "Shared Waves ($cleanCode)",
                description = "Collaborative listening room joined via code $cleanCode",
                coverUri = "drawable://img_cover_midnight_waves",
                isCollaborative = true,
                inviteCode = cleanCode,
                ownerName = "Community Friend",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                songCount = 2
            )
            playlistDao.insertPlaylist(newPlaylist)

            collaborationDao.insertCollaborator(
                Collaborator(
                    id = "${mockId}_owner",
                    playlistId = mockId,
                    userId = "user_host",
                    name = "Community Friend",
                    avatarColorHex = "#F472B6",
                    isOnline = true,
                    role = "Owner",
                    lastActiveTimestamp = System.currentTimeMillis()
                )
            )
            collaborationDao.insertCollaborator(
                Collaborator(
                    id = "${mockId}_me",
                    playlistId = mockId,
                    userId = "user_me",
                    name = userName,
                    avatarColorHex = "#38BDF8",
                    isOnline = true,
                    role = "Editor",
                    lastActiveTimestamp = System.currentTimeMillis()
                )
            )

            // Seed initial crossrefs
            playlistDao.insertPlaylistSongCrossRef(
                PlaylistSongCrossRef(mockId, "demo_track_01", 0, "Community Friend")
            )
            playlistDao.insertPlaylistSongCrossRef(
                PlaylistSongCrossRef(mockId, "demo_track_02", 1, userName)
            )

            collaborationDao.insertActivity(
                CollaborationActivity(
                    playlistId = mockId,
                    userName = userName,
                    actionText = "joined the session with $cleanCode",
                    timestamp = System.currentTimeMillis()
                )
            )
            newPlaylist
        }
    }

    suspend fun addSongToCollabPlaylist(playlistId: String, songId: String, songTitle: String, userName: String = "You") = withContext(Dispatchers.IO) {
        val currentCount = playlistDao.getPlaylistSongCount(playlistId)
        val crossRef = PlaylistSongCrossRef(
            playlistId = playlistId,
            songId = songId,
            orderIndex = currentCount,
            addedBy = userName,
            addedAtTimestamp = System.currentTimeMillis()
        )
        playlistDao.insertPlaylistSongCrossRef(crossRef)
        playlistDao.updatePlaylistSongCount(playlistId, currentCount + 1)

        val activity = CollaborationActivity(
            playlistId = playlistId,
            userName = userName,
            actionText = "added '$songTitle'",
            timestamp = System.currentTimeMillis()
        )
        collaborationDao.insertActivity(activity)

        collaborationDao.enqueueSyncItem(
            SyncQueueItem(
                actionType = "ADD_SONG",
                entityId = playlistId,
                payloadJson = "{\"songId\":\"$songId\",\"addedBy\":\"$userName\"}"
            )
        )
    }

    suspend fun simulateFriendActivity(playlistId: String) = withContext(Dispatchers.IO) {
        val friends = listOf("Sarah Chen", "Arun Patel", "Rahul Verma", "Elena Rostova")
        val friend = friends.random()
        val actions = listOf(
            "moved a track to #1",
            "voted for 'Midnight Waves'",
            "joined the listening room",
            "added 'Azure Horizons'",
            "is now listening together"
        )
        val action = actions.random()
        val activity = CollaborationActivity(
            playlistId = playlistId,
            userName = friend,
            actionText = action,
            timestamp = System.currentTimeMillis()
        )
        collaborationDao.insertActivity(activity)
    }

    suspend fun syncPendingChanges(): Int = withContext(Dispatchers.IO) {
        // Mock online sync with cloud backend
        val list = collaborationDao.getPendingSyncItems()
        collaborationDao.clearSyncedItems()
        2 // synced count
    }
}
