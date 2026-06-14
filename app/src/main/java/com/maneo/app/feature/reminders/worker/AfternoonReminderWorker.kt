package com.maneo.app.feature.reminders.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AfternoonReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val getVerseForSlot: GetVerseForSlot,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val verse = getVerseForSlot("afternoon")
        val preview = if (verse.text.length > 100) verse.text.take(100) + "…" else verse.text
        NotificationHelper.sendReminder(
            context = applicationContext,
            title = "A midday pause",
            body = preview,
            slot = "afternoon",
        )
        return Result.success()
    }
}
