package com.maneo.app.feature.journal.domain

import com.maneo.app.core.domain.model.JournalEntry
import com.maneo.app.feature.journal.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEntries @Inject constructor(private val repository: JournalRepository) {
    operator fun invoke(): Flow<List<JournalEntry>> = repository.getEntries()
}
