package com.maneo.app.feature.reminders.worker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maneo.app.core.data.prefs.PrefsKeys
import com.maneo.app.core.data.prefs.SeenVerseRepository
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class AfternoonReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getVerseForSlot: GetVerseForSlot,
    private val seenVerseRepository: SeenVerseRepository,
    private val dataStore: DataStore<Preferences>,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = "afternoon"
        val date = LocalDate.now()
        val translation = dataStore.data.first()[PrefsKeys.TRANSLATION] ?: "web"
        val seenIds = seenVerseRepository.getSeenVerseIds(slot, date)
        val verse = getVerseForSlot(slot, translation = translation, seenIds = seenIds)
        seenVerseRepository.markVerseSeen(slot, verse.id, date)
        val preview = if (verse.text.length > 100) verse.text.take(100) + "…" else verse.text
        NotificationHelper.sendReminder(
            context = applicationContext,
            title = "A midday pause",
            body = preview,
            slot = slot,
        )
        return Result.success()
    }
}
