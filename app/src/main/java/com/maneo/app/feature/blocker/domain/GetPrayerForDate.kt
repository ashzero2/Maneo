package com.maneo.app.feature.blocker.domain

import com.maneo.app.feature.blocker.repository.PrayerRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

class GetPrayerForDate @Inject constructor(private val repository: PrayerRepository) {

    operator fun invoke(date: LocalDate = LocalDate.now()): Prayer {
        val pool = repository.prayers
        require(pool.isNotEmpty()) { "No prayers loaded" }
        return pool[abs(date.toEpochDay().toInt()) % pool.size]
    }
}
