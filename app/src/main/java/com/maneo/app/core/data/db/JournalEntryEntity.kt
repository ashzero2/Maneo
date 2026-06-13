package com.maneo.app.core.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.maneo.app.core.domain.model.JournalEntry

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val promptUsed: String?,
    val slot: String?,
    val createdAt: Long,
)

fun JournalEntryEntity.toDomain() = JournalEntry(
    id        = id,
    text      = text,
    promptUsed = promptUsed,
    slot      = slot,
    createdAt = createdAt,
)

fun JournalEntry.toEntity() = JournalEntryEntity(
    id        = id,
    text      = text,
    promptUsed = promptUsed,
    slot      = slot,
    createdAt = createdAt,
)
