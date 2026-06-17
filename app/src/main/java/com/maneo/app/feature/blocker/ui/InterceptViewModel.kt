package com.maneo.app.feature.blocker.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.PrefsKeys
import com.maneo.app.core.data.prefs.SeenVerseRepository
import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.blocker.domain.GetPrayerForDate
import com.maneo.app.feature.blocker.domain.Prayer
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class InterceptViewModel @Inject constructor(
    private val getVerseForSlot: GetVerseForSlot,
    private val getPrayerForDate: GetPrayerForDate,
    private val seenVerseRepository: SeenVerseRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _verse = MutableStateFlow<Verse?>(null)
    val verse: StateFlow<Verse?> = _verse.asStateFlow()

    private val _prayer = MutableStateFlow<Prayer?>(null)
    val prayer: StateFlow<Prayer?> = _prayer.asStateFlow()

    private val _timerEnabled = MutableStateFlow(false)
    val timerEnabled: StateFlow<Boolean> = _timerEnabled.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(10)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val slot = "intercept"
            val date = LocalDate.now()

            // Escalation: if the user has opened blocked apps many times today, shift to grounding tone
            val todayCount = getTodayInterceptCount(prefs, date)
            incrementTodayInterceptCount(date)
            val tone = if (todayCount >= ESCALATION_THRESHOLD) "grounding" else null

            val seenVerseIds = seenVerseRepository.getSeenVerseIds(slot, date)
            val v = getVerseForSlot(slot, tone = tone, seenIds = seenVerseIds)
            seenVerseRepository.markVerseSeen(slot, v.id, date)
            _verse.value = v

            val seenPrayerIds = seenVerseRepository.getSeenPrayerIds(date)
            val p = getPrayerForDate(seenIds = seenPrayerIds)
            seenVerseRepository.markPrayerSeen(p.id, date)
            _prayer.value = p

            val enabled = prefs[PrefsKeys.INTERCEPT_TIMER_ENABLED] ?: false
            val seconds = prefs[PrefsKeys.INTERCEPT_TIMER_SECONDS] ?: 10
            _timerEnabled.value = enabled
            _timerTotalSeconds.value = seconds

            if (enabled) {
                var remaining = seconds
                while (remaining > 0) {
                    _remainingSeconds.value = remaining
                    delay(1_000)
                    remaining--
                }
                _remainingSeconds.value = 0
            }
        }
    }

    fun recordWait() {
        viewModelScope.launch { incrementWeeklyCount(isWait = true) }
    }

    fun recordContinue() {
        viewModelScope.launch { incrementWeeklyCount(isWait = false) }
    }

    private fun getTodayInterceptCount(prefs: Preferences, date: LocalDate): Int {
        val storedDay = prefs[PrefsKeys.TODAY_INTERCEPT_DATE] ?: 0L
        if (date.toEpochDay() != storedDay) return 0
        return prefs[PrefsKeys.TODAY_INTERCEPT_COUNT] ?: 0
    }

    private suspend fun incrementTodayInterceptCount(date: LocalDate) {
        dataStore.edit { prefs ->
            val today = date.toEpochDay()
            val storedDay = prefs[PrefsKeys.TODAY_INTERCEPT_DATE] ?: 0L
            if (today != storedDay) {
                prefs[PrefsKeys.TODAY_INTERCEPT_DATE] = today
                prefs[PrefsKeys.TODAY_INTERCEPT_COUNT] = 1
            } else {
                prefs[PrefsKeys.TODAY_INTERCEPT_COUNT] = (prefs[PrefsKeys.TODAY_INTERCEPT_COUNT] ?: 0) + 1
            }
        }
    }

    private suspend fun incrementWeeklyCount(isWait: Boolean) {
        dataStore.edit { prefs ->
            val today = LocalDate.now().toEpochDay()
            val resetDay = prefs[PrefsKeys.WEEKLY_COUNT_RESET_EPOCH_DAY] ?: today
            if (today - resetDay >= 7) {
                prefs[PrefsKeys.WEEKLY_COUNT_RESET_EPOCH_DAY] = today
                prefs[PrefsKeys.WEEKLY_WAIT_COUNT] = if (isWait) 1 else 0
                prefs[PrefsKeys.WEEKLY_CONTINUE_COUNT] = if (!isWait) 1 else 0
            } else {
                if (isWait) {
                    prefs[PrefsKeys.WEEKLY_WAIT_COUNT] = (prefs[PrefsKeys.WEEKLY_WAIT_COUNT] ?: 0) + 1
                } else {
                    prefs[PrefsKeys.WEEKLY_CONTINUE_COUNT] = (prefs[PrefsKeys.WEEKLY_CONTINUE_COUNT] ?: 0) + 1
                }
            }
        }
    }

    private companion object {
        const val ESCALATION_THRESHOLD = 10
    }
}
