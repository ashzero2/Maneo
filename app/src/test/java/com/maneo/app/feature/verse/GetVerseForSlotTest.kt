package com.maneo.app.feature.verse

import com.maneo.app.core.domain.model.Verse
import com.maneo.app.feature.verse.domain.GetVerseForSlot
import com.maneo.app.feature.verse.repository.VerseRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetVerseForSlotTest {

    private val interceptVerses = (1..10).map { i ->
        Verse("i$i", "Ref $i:$i", "Intercept text $i", listOf("intercept"), listOf("grounding"))
    }
    private val morningVerses = (1..10).map { i ->
        Verse("m$i", "Morn $i:$i", "Morning text $i", listOf("morning"), listOf("inviting"))
    }
    private val allVerses = interceptVerses + morningVerses

    private lateinit var useCase: GetVerseForSlot

    @Before
    fun setup() {
        val repo = VerseRepository { allVerses }
        useCase = GetVerseForSlot(repo)
    }

    @Test
    fun sameDay_sameSlot_returnsSameVerse() {
        val date = LocalDate.of(2024, 1, 1)
        val first = useCase("intercept", date)
        val second = useCase("intercept", date)
        assertEquals(first, second)
    }

    @Test
    fun sameDay_differentSlots_returnVerseFromCorrectPool() {
        val date = LocalDate.of(2024, 1, 1)
        val interceptVerse = useCase("intercept", date)
        val morningVerse = useCase("morning", date)

        assertTrue("intercept" in interceptVerse.slots)
        assertTrue("morning" in morningVerse.slots)
    }

    @Test
    fun differentDays_returnValidVerseFromPool() {
        val pool = interceptVerses
        for (day in 0L..29L) {
            val date = LocalDate.of(2024, 1, 1).plusDays(day)
            val verse = useCase("intercept", date)
            assertNotNull(verse)
            assertTrue("verse not from intercept pool", pool.contains(verse))
        }
    }

    @Test
    fun allSlotsReturnFromCorrectPool() {
        val date = LocalDate.of(2024, 6, 15)
        val interceptResult = useCase("intercept", date)
        val morningResult = useCase("morning", date)

        assertTrue(interceptVerses.contains(interceptResult))
        assertTrue(morningVerses.contains(morningResult))
    }
}
