package com.maneo.app.core.domain.model

import kotlinx.serialization.Serializable

// Spec §5 — @Serializable added for Phase 2 JSON parsing via kotlinx.serialization
@Serializable
data class Verse(
    val id: String,
    val reference: String,
    val text: String,
    val slots: List<String>,    // "morning", "afternoon", "evening", "intercept"
    val tone: List<String>,     // "inviting", "grounding"
)
