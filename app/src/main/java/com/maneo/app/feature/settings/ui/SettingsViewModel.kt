package com.maneo.app.feature.settings.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.PrefsKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    val thresholdMins: StateFlow<Int> = dataStore.data
        .map { it[PrefsKeys.SCREEN_TIME_THRESHOLD_MINS] ?: 30 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 30)

    val timerEnabled: StateFlow<Boolean> = dataStore.data
        .map { it[PrefsKeys.INTERCEPT_TIMER_ENABLED] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val timerSeconds: StateFlow<Int> = dataStore.data
        .map { it[PrefsKeys.INTERCEPT_TIMER_SECONDS] ?: 10 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 10)

    fun setThreshold(minutes: Int) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.SCREEN_TIME_THRESHOLD_MINS] = minutes }
        }
    }

    fun setTimerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.INTERCEPT_TIMER_ENABLED] = enabled }
        }
    }

    fun setTimerSeconds(seconds: Int) {
        viewModelScope.launch {
            dataStore.edit { it[PrefsKeys.INTERCEPT_TIMER_SECONDS] = seconds }
        }
    }
}
