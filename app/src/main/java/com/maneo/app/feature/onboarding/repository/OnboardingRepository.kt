package com.maneo.app.feature.onboarding.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.maneo.app.core.data.prefs.PrefsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isOnboardingDone: Flow<Boolean> = dataStore.data.map { it[PrefsKeys.ONBOARDING_DONE] ?: false }

    suspend fun completeOnboarding() {
        dataStore.edit { it[PrefsKeys.ONBOARDING_DONE] = true }
    }
}
