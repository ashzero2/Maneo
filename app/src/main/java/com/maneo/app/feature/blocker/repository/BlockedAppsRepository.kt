package com.maneo.app.feature.blocker.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.maneo.app.core.data.prefs.PrefsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val blockedApps: Flow<Set<String>> = dataStore.data
        .map { it[PrefsKeys.BLOCKED_APPS] ?: emptySet() }

    suspend fun setBlocked(packageName: String, blocked: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[PrefsKeys.BLOCKED_APPS] ?: emptySet()
            prefs[PrefsKeys.BLOCKED_APPS] = if (blocked) current + packageName else current - packageName
        }
    }
}
