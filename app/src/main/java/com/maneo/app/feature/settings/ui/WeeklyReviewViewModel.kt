package com.maneo.app.feature.settings.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.PrefsKeys
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WeeklyReviewViewModel @Inject constructor(
    dataStore: DataStore<Preferences>,
) : ViewModel() {

    val waitCount: StateFlow<Int> = dataStore.data
        .map { prefs ->
            if (isExpired(prefs)) return@map 0
            prefs[PrefsKeys.WEEKLY_WAIT_COUNT] ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val continueCount: StateFlow<Int> = dataStore.data
        .map { prefs ->
            if (isExpired(prefs)) return@map 0
            prefs[PrefsKeys.WEEKLY_CONTINUE_COUNT] ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private fun isExpired(prefs: Preferences): Boolean {
        val resetDay = prefs[PrefsKeys.WEEKLY_COUNT_RESET_EPOCH_DAY] ?: return true
        return LocalDate.now().toEpochDay() - resetDay >= 7
    }
}
