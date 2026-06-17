package com.maneo.app.core.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeenVerseRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getSeenVerseIds(slot: String, date: LocalDate): Set<String> {
        val prefs = dataStore.data.first()
        val resetDay = prefs[PrefsKeys.seenVerseResetDay(slot)] ?: 0L
        if (date.toEpochDay() - resetDay >= VERSE_WINDOW_DAYS) return emptySet()
        return prefs[PrefsKeys.seenVerseIds(slot)] ?: emptySet()
    }

    suspend fun markVerseSeen(slot: String, verseId: String, date: LocalDate) {
        dataStore.edit { prefs ->
            val currentDay = date.toEpochDay()
            val resetDay = prefs[PrefsKeys.seenVerseResetDay(slot)] ?: 0L
            if (currentDay - resetDay >= VERSE_WINDOW_DAYS) {
                prefs[PrefsKeys.seenVerseResetDay(slot)] = currentDay
                prefs[PrefsKeys.seenVerseIds(slot)] = setOf(verseId)
            } else {
                val existing = prefs[PrefsKeys.seenVerseIds(slot)] ?: emptySet()
                prefs[PrefsKeys.seenVerseIds(slot)] = existing + verseId
            }
        }
    }

    suspend fun getSeenPrayerIds(date: LocalDate): Set<String> {
        val prefs = dataStore.data.first()
        val resetDay = prefs[PrefsKeys.SEEN_PRAYER_RESET_DAY] ?: 0L
        if (date.toEpochDay() - resetDay >= PRAYER_WINDOW_DAYS) return emptySet()
        return prefs[PrefsKeys.SEEN_PRAYER_IDS] ?: emptySet()
    }

    suspend fun markPrayerSeen(prayerId: String, date: LocalDate) {
        dataStore.edit { prefs ->
            val currentDay = date.toEpochDay()
            val resetDay = prefs[PrefsKeys.SEEN_PRAYER_RESET_DAY] ?: 0L
            if (currentDay - resetDay >= PRAYER_WINDOW_DAYS) {
                prefs[PrefsKeys.SEEN_PRAYER_RESET_DAY] = currentDay
                prefs[PrefsKeys.SEEN_PRAYER_IDS] = setOf(prayerId)
            } else {
                val existing = prefs[PrefsKeys.SEEN_PRAYER_IDS] ?: emptySet()
                prefs[PrefsKeys.SEEN_PRAYER_IDS] = existing + prayerId
            }
        }
    }

    private companion object {
        const val VERSE_WINDOW_DAYS = 14L
        const val PRAYER_WINDOW_DAYS = 7L
    }
}
