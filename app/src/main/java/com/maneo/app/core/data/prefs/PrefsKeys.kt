package com.maneo.app.core.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

// Spec §4 — all DataStore keys in one place; feature repos reference these, not raw strings
object PrefsKeys {
    val BLOCKED_APPS               = stringSetPreferencesKey("blocked_apps")
    val ONBOARDING_DONE            = booleanPreferencesKey("onboarding_done")
    val SCREEN_TIME_THRESHOLD_MINS = intPreferencesKey("screen_time_threshold_mins")

    // Reminder slots — stored as "HH:mm" string + enabled bool (Spec §4)
    val MORNING_TIME      = stringPreferencesKey("reminder_morning_time")
    val MORNING_ENABLED   = booleanPreferencesKey("reminder_morning_enabled")
    val AFTERNOON_TIME    = stringPreferencesKey("reminder_afternoon_time")
    val AFTERNOON_ENABLED = booleanPreferencesKey("reminder_afternoon_enabled")
    val EVENING_TIME      = stringPreferencesKey("reminder_evening_time")
    val EVENING_ENABLED   = booleanPreferencesKey("reminder_evening_enabled")

    // Screen time: "packageName:yyyy-MM-dd" entries — cleared when date changes (Spec §4)
    val SCREEN_TIME_NOTIFIED = stringSetPreferencesKey("screen_time_notified")

    // Seen verse de-dup — 14-day rolling window per slot
    val SEEN_VERSE_IDS_MORNING         = stringSetPreferencesKey("seen_verse_ids_morning")
    val SEEN_VERSE_IDS_AFTERNOON       = stringSetPreferencesKey("seen_verse_ids_afternoon")
    val SEEN_VERSE_IDS_EVENING         = stringSetPreferencesKey("seen_verse_ids_evening")
    val SEEN_VERSE_IDS_INTERCEPT       = stringSetPreferencesKey("seen_verse_ids_intercept")
    val SEEN_VERSE_RESET_DAY_MORNING   = longPreferencesKey("seen_verse_reset_day_morning")
    val SEEN_VERSE_RESET_DAY_AFTERNOON = longPreferencesKey("seen_verse_reset_day_afternoon")
    val SEEN_VERSE_RESET_DAY_EVENING   = longPreferencesKey("seen_verse_reset_day_evening")
    val SEEN_VERSE_RESET_DAY_INTERCEPT = longPreferencesKey("seen_verse_reset_day_intercept")

    // Seen prayer de-dup — 7-day rolling window
    val SEEN_PRAYER_IDS      = stringSetPreferencesKey("seen_prayer_ids")
    val SEEN_PRAYER_RESET_DAY = longPreferencesKey("seen_prayer_reset_day")

    // Intercept timer — optional dwell before buttons appear
    val INTERCEPT_TIMER_ENABLED = booleanPreferencesKey("intercept_timer_enabled")
    val INTERCEPT_TIMER_SECONDS = intPreferencesKey("intercept_timer_seconds")

    // Intercept outcomes — weekly rolling counts
    val WEEKLY_WAIT_COUNT           = intPreferencesKey("weekly_wait_count")
    val WEEKLY_CONTINUE_COUNT       = intPreferencesKey("weekly_continue_count")
    val WEEKLY_COUNT_RESET_EPOCH_DAY = longPreferencesKey("weekly_count_reset_epoch_day")

    // Today's intercept count — for gentle tone escalation
    val TODAY_INTERCEPT_COUNT = intPreferencesKey("today_intercept_count")
    val TODAY_INTERCEPT_DATE  = longPreferencesKey("today_intercept_date")

    fun seenVerseIds(slot: String) = when (slot) {
        "morning"   -> SEEN_VERSE_IDS_MORNING
        "afternoon" -> SEEN_VERSE_IDS_AFTERNOON
        "evening"   -> SEEN_VERSE_IDS_EVENING
        else        -> SEEN_VERSE_IDS_INTERCEPT
    }

    fun seenVerseResetDay(slot: String) = when (slot) {
        "morning"   -> SEEN_VERSE_RESET_DAY_MORNING
        "afternoon" -> SEEN_VERSE_RESET_DAY_AFTERNOON
        "evening"   -> SEEN_VERSE_RESET_DAY_EVENING
        else        -> SEEN_VERSE_RESET_DAY_INTERCEPT
    }
}
