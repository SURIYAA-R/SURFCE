package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CollaborationActivity
import com.example.data.model.Collaborator
import com.example.data.model.Playlist
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.Song
import com.example.data.model.SyncQueueItem
import com.example.data.model.UserSettings

@Database(
    entities = [
        Song::class,
        Playlist::class,
        PlaylistSongCrossRef::class,
        Collaborator::class,
        CollaborationActivity::class,
        SyncQueueItem::class,
        UserSettings::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WavyxDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun collaborationDao(): CollaborationDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: WavyxDatabase? = null

        fun getDatabase(context: Context): WavyxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WavyxDatabase::class.java,
                    "wavyx_music_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
