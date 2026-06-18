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
        translation: String = "web",
        seenIds: Set<String> = emptySet(),
        date: LocalDate = LocalDate.now(),
    ): Verse {
        val allPool = repository.getVersesForSlot(slot)
        require(allPool.isNotEmpty()) { "No verses for slot: $slot" }

        val translationPool = allPool.filter { it.translation == translation }.takeIf { it.isNotEmpty() } ?: allPool

        val toneFilter = tone ?: defaultTone(slot)
        val tonedPool = translationPool.filter { toneFilter in it.tone }.takeIf { it.size >= 3 } ?: translationPool
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
