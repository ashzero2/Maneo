package com.maneo.app.feature.home.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.PrefsKeys
import com.maneo.app.core.data.prefs.SeenVerseRepository
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.repository.BlockedAppsRepository
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVerseForSlot: GetVerseForSlot,
    private val seenVerseRepository: SeenVerseRepository,
    private val dataStore: DataStore<Preferences>,
    blockedAppsRepository: BlockedAppsRepository,
) : ViewModel() {

    private val _verse = MutableStateFlow<Verse?>(null)
    val verse: StateFlow<Verse?> = _verse.asStateFlow()

    val blockedCount: StateFlow<Int> = blockedAppsRepository.blockedApps
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val weeklyWaitCount: StateFlow<Int> = dataStore.data
        .map { prefs ->
            val resetDay = prefs[PrefsKeys.WEEKLY_COUNT_RESET_EPOCH_DAY] ?: return@map 0
            if (LocalDate.now().toEpochDay() - resetDay >= 7) return@map 0
            prefs[PrefsKeys.WEEKLY_WAIT_COUNT] ?: 0
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            val slot = "morning"
            val date = LocalDate.now()
            val seenIds = seenVerseRepository.getSeenVerseIds(slot, date)
            val v = getVerseForSlot(slot, seenIds = seenIds)
            seenVerseRepository.markVerseSeen(slot, v.id, date)
            _verse.value = v
        }
    }
}
