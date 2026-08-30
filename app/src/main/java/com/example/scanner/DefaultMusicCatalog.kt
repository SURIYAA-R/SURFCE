package com.example.scanner

import com.example.data.model.CollaborationActivity
import com.example.data.model.Collaborator
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song

/**
 * SURFCE relies purely on local device audio files.
 * Inbuilt/demo song files are removed to ensure all views reflect local storage.
 */
object DefaultMusicCatalog {
    fun getInitialSongs(): List<Song> {
        return emptyList()
    }

    fun getInitialPlaylists(): List<Playlist> {
        return emptyList()
    }

    fun getInitialPlaylistCrossRefs(): List<PlaylistSongCrossRef> {
        return emptyList()
    }

    fun getInitialCollaborators(): List<Collaborator> {
        return emptyList()
    }

    fun getInitialActivities(): List<CollaborationActivity> {
        return emptyList()
    }
}
