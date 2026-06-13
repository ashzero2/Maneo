package com.maneo.app.core.domain.model

// Spec §5 — plain domain model, no framework annotations
data class JournalEntry(
    val id: Long = 0,
    val text: String,
    val promptUsed: String?,    // null if user dismissed the prompt
    val slot: String?,          // "morning" / "afternoon" / "evening" / null (manual entry)
    val createdAt: Long,        // epoch millis
)
