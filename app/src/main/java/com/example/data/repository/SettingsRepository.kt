package com.example.data.repository

import android.content.Context
import com.example.data.local.WavyxDatabase
import com.example.data.model.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SettingsRepository(context: Context) {
    private val database = WavyxDatabase.getDatabase(context)
    private val userSettingsDao = database.userSettingsDao()

    val userSettings: Flow<UserSettings?> = userSettingsDao.getUserSettings()

    suspend fun updateOnlineSync(enabled: Boolean) = withContext(Dispatchers.IO) {
        userSettingsDao.setOnlineSyncEnabled(enabled)
    }

    suspend fun updateUserName(name: String) = withContext(Dispatchers.IO) {
        userSettingsDao.updateUserName(name)
    }

    suspend fun updateEqualizer(preset: String) = withContext(Dispatchers.IO) {
        userSettingsDao.updateEqualizerPreset(preset)
    }

    suspend fun updateSleepTimer(minutes: Int) = withContext(Dispatchers.IO) {
        userSettingsDao.updateSleepTimer(minutes)
    }

    suspend fun updateThemeMode(mode: String) = withContext(Dispatchers.IO) {
        userSettingsDao.updateThemeMode(mode)
    }
}
