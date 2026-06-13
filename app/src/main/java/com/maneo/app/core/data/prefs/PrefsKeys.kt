package com.maneo.app.core.data.prefs

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
}
