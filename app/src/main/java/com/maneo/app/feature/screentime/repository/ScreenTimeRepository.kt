package com.maneo.app.feature.screentime.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.maneo.app.core.data.prefs.PrefsKeys
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun getThresholdMins(): Int =
        dataStore.data.first()[PrefsKeys.SCREEN_TIME_THRESHOLD_MINS] ?: 30

    suspend fun isNotifiedToday(packageName: String, today: LocalDate): Boolean {
        val key = "$packageName:$today"
        val entries = dataStore.data.first()[PrefsKeys.SCREEN_TIME_NOTIFIED] ?: emptySet()
        return key in entries
    }

    suspend fun markNotifiedToday(packageName: String, today: LocalDate) {
        val todayStr = today.toString()
        val key = "$packageName:$todayStr"
        dataStore.edit { prefs ->
            val current = prefs[PrefsKeys.SCREEN_TIME_NOTIFIED] ?: emptySet()
            // Drop entries from previous days; keep only today's
            val todayOnly = current.filter { it.endsWith(":$todayStr") }.toSet()
            prefs[PrefsKeys.SCREEN_TIME_NOTIFIED] = todayOnly + key
        }
    }
}
