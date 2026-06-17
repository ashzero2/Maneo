package com.maneo.app.feature.journal.domain

import com.maneo.app.feature.journal.repository.JournalRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportJournal @Inject constructor(private val repository: JournalRepository) {

    private val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a")

    suspend operator fun invoke(): String {
        val entries = repository.getEntries().first()
        if (entries.isEmpty()) return ""
        return entries.joinToString("\n\n---\n\n") { entry ->
            val date = Instant.ofEpochMilli(entry.createdAt)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
            buildString {
                appendLine(date)
                if (!entry.promptUsed.isNullOrBlank()) appendLine("Prompt: ${entry.promptUsed}")
                append(entry.text)
            }
        }
    }
}
