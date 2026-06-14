package com.maneo.app.feature.journal.repository

import com.maneo.app.core.data.db.JournalDao
import com.maneo.app.core.data.db.toDomain
import com.maneo.app.core.data.db.toEntity
import com.maneo.app.core.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalRepository @Inject constructor(private val dao: JournalDao) {

    fun getEntries(): Flow<List<JournalEntry>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveEntry(entry: JournalEntry) {
        dao.insert(entry.toEntity())
    }
}
