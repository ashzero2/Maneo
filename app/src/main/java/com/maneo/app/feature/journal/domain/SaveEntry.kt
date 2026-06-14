package com.maneo.app.feature.journal.domain

import com.maneo.app.core.domain.model.JournalEntry
import com.maneo.app.feature.journal.repository.JournalRepository
import javax.inject.Inject

class SaveEntry @Inject constructor(private val repository: JournalRepository) {
    suspend operator fun invoke(text: String, promptUsed: String?, slot: String?) {
        require(text.isNotBlank()) { "Journal text cannot be blank" }
        repository.saveEntry(
            JournalEntry(
                text = text.trim(),
                promptUsed = promptUsed,
                slot = slot,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
