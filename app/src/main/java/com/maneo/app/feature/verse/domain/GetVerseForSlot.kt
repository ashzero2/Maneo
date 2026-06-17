package com.maneo.app.feature.verse.domain

import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.verse.repository.VerseRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

class GetVerseForSlot @Inject constructor(private val repository: VerseRepository) {

    operator fun invoke(
        slot: String,
        tone: String? = null,
        seenIds: Set<String> = emptySet(),
        date: LocalDate = LocalDate.now(),
    ): Verse {
        val fullPool = repository.getVersesForSlot(slot)
        require(fullPool.isNotEmpty()) { "No verses for slot: $slot" }

        val toneFilter = tone ?: defaultTone(slot)
        val tonedPool = fullPool.filter { toneFilter in it.tone }.takeIf { it.size >= 3 } ?: fullPool
        val freshPool = tonedPool.filter { it.id !in seenIds }.takeIf { it.size >= 3 } ?: tonedPool

        val seed = date.toEpochDay() * 31L + slot.hashCode()
        return freshPool[abs(seed).toInt() % freshPool.size]
    }

    private fun defaultTone(slot: String): String = when (slot) {
        "morning"   -> "inviting"
        "afternoon" -> "grounding"
        "evening"   -> "grounding"
        else        -> "inviting"
    }
}
