package com.maneo.app.feature.verse.domain

import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.verse.repository.VerseRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

class GetVerseForSlot @Inject constructor(private val repository: VerseRepository) {

    operator fun invoke(slot: String, date: LocalDate = LocalDate.now()): Verse {
        val pool = repository.getVersesForSlot(slot)
        require(pool.isNotEmpty()) { "No verses for slot: $slot" }
        val seed = date.toEpochDay() * 31L + slot.hashCode()
        return pool[abs(seed).toInt() % pool.size]
    }
}
