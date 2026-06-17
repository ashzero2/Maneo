package com.maneo.app.feature.reminders.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maneo.app.core.data.prefs.SeenVerseRepository
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class MorningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getVerseForSlot: GetVerseForSlot,
    private val seenVerseRepository: SeenVerseRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val slot = "morning"
        val date = LocalDate.now()
        val seenIds = seenVerseRepository.getSeenVerseIds(slot, date)
        val verse = getVerseForSlot(slot, seenIds = seenIds)
        seenVerseRepository.markVerseSeen(slot, verse.id, date)
        val preview = if (verse.text.length > 100) verse.text.take(100) + "…" else verse.text
        NotificationHelper.sendReminder(
            context = applicationContext,
            title = "Good morning",
            body = preview,
            slot = slot,
        )
        return Result.success()
    }
}
