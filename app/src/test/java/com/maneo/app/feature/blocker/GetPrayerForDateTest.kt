package com.maneo.app.feature.blocker

import com.maneo.app.feature.blocker.domain.GetPrayerForDate
import com.maneo.app.feature.blocker.domain.Prayer
import com.maneo.app.feature.blocker.repository.PrayerRepository
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetPrayerForDateTest {

    private val prayers = (1..10).map { i -> Prayer("p$i", "Prayer text $i") }
    private lateinit var useCase: GetPrayerForDate

    @Before
    fun setup() {
        useCase = GetPrayerForDate(PrayerRepository { prayers })
    }

    @Test
    fun sameDay_returnsSamePrayer() {
        val date = LocalDate.of(2024, 1, 1)
        val first = useCase(date = date)
        val second = useCase(date = date)
        assertTrue(first.id == second.id)
    }

    @Test
    fun differentDays_returnValidPrayer() {
        for (day in 0L..29L) {
            val date = LocalDate.of(2024, 1, 1).plusDays(day)
            val result = useCase(date = date)
            assertNotNull(result)
            assertTrue(prayers.contains(result))
        }
    }

    @Test
    fun seenIds_excludedWhenFreshPoolLargeEnough() {
        val date = LocalDate.of(2024, 2, 1)
        val first = useCase(date = date)
        val second = useCase(seenIds = setOf(first.id), date = date)
        // pool has 10 items, fresh pool has 9 — well above threshold of 3
        assertNotNull(second)
        assertTrue(second.id != first.id)
    }

    @Test
    fun seenIds_fallsBackToFullPoolWhenFreshTooSmall() {
        val date = LocalDate.of(2024, 3, 1)
        // mark 9 of 10 seen — fresh pool is only 1, below threshold
        val almostAll = prayers.take(9).map { it.id }.toSet()
        val result = useCase(seenIds = almostAll, date = date)
        assertNotNull(result)
        assertTrue(prayers.contains(result))
    }

    @Test
    fun emptySeenIds_returnsFromFullPool() {
        val result = useCase(seenIds = emptySet(), date = LocalDate.of(2024, 6, 1))
        assertNotNull(result)
        assertTrue(prayers.contains(result))
    }
}
