package com.maneo.app.core.domain.model

// Spec §5 — plain domain model, no framework annotations
data class ReminderSlot(
    val slot: String,       // "morning" / "afternoon" / "evening"
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
)
