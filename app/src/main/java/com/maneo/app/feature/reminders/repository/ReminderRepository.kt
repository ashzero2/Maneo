package com.maneo.app.feature.reminders.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.maneo.app.core.data.prefs.PrefsKeys
import com.maneo.app.feature.reminders.worker.AfternoonReminderWorker
import com.maneo.app.feature.reminders.worker.EveningReminderWorker
import com.maneo.app.feature.reminders.worker.MorningReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

data class ReminderSettings(
    val morningEnabled: Boolean,
    val morningTime: String,
    val afternoonEnabled: Boolean,
    val afternoonTime: String,
    val eveningEnabled: Boolean,
    val eveningTime: String,
) {
    companion object {
        val DEFAULT = ReminderSettings(
            morningEnabled = false,
            morningTime = "07:00",
            afternoonEnabled = false,
            afternoonTime = "12:30",
            eveningEnabled = false,
            eveningTime = "20:00",
        )
    }
}

@Singleton
class ReminderRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) {

    val settings: Flow<ReminderSettings> = dataStore.data.map { prefs ->
        ReminderSettings(
            morningEnabled = prefs[PrefsKeys.MORNING_ENABLED] ?: false,
            morningTime = prefs[PrefsKeys.MORNING_TIME] ?: "07:00",
            afternoonEnabled = prefs[PrefsKeys.AFTERNOON_ENABLED] ?: false,
            afternoonTime = prefs[PrefsKeys.AFTERNOON_TIME] ?: "12:30",
            eveningEnabled = prefs[PrefsKeys.EVENING_ENABLED] ?: false,
            eveningTime = prefs[PrefsKeys.EVENING_TIME] ?: "20:00",
        )
    }

    suspend fun setSlot(slot: String, enabled: Boolean, time: String) {
        dataStore.edit { prefs ->
            when (slot) {
                "morning" -> { prefs[PrefsKeys.MORNING_ENABLED] = enabled; prefs[PrefsKeys.MORNING_TIME] = time }
                "afternoon" -> { prefs[PrefsKeys.AFTERNOON_ENABLED] = enabled; prefs[PrefsKeys.AFTERNOON_TIME] = time }
                "evening" -> { prefs[PrefsKeys.EVENING_ENABLED] = enabled; prefs[PrefsKeys.EVENING_TIME] = time }
            }
        }
        scheduleSlot(slot, time, enabled)
    }

    suspend fun rescheduleAll() {
        val prefs = dataStore.data.first()
        scheduleSlot("morning", prefs[PrefsKeys.MORNING_TIME] ?: "07:00", prefs[PrefsKeys.MORNING_ENABLED] ?: false)
        scheduleSlot("afternoon", prefs[PrefsKeys.AFTERNOON_TIME] ?: "12:30", prefs[PrefsKeys.AFTERNOON_ENABLED] ?: false)
        scheduleSlot("evening", prefs[PrefsKeys.EVENING_TIME] ?: "20:00", prefs[PrefsKeys.EVENING_ENABLED] ?: false)
    }

    private fun scheduleSlot(slot: String, time: String, enabled: Boolean) {
        val wm = WorkManager.getInstance(context)
        if (!enabled) {
            wm.cancelUniqueWork("reminder_$slot")
            return
        }
        val delayMs = calculateDelayMs(time)
        val request = when (slot) {
            "morning" -> PeriodicWorkRequestBuilder<MorningReminderWorker>(24, TimeUnit.HOURS)
            "afternoon" -> PeriodicWorkRequestBuilder<AfternoonReminderWorker>(24, TimeUnit.HOURS)
            else -> PeriodicWorkRequestBuilder<EveningReminderWorker>(24, TimeUnit.HOURS)
        }
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .addTag("reminder_$slot")
            .build()
        wm.enqueueUniquePeriodicWork("reminder_$slot", ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, request)
    }

    private fun calculateDelayMs(time: String): Long {
        val (h, m) = time.split(":").map { it.toInt() }
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(h, m)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return ChronoUnit.MILLIS.between(now, target)
    }
}
