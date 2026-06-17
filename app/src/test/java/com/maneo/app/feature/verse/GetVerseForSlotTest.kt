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
        val first = useCase(slot = "intercept", date = date)
        val second = useCase(slot = "intercept", date = date)
        assertEquals(first, second)
    }

    @Test
    fun sameDay_differentSlots_returnVerseFromCorrectPool() {
        val date = LocalDate.of(2024, 1, 1)
        val interceptVerse = useCase(slot = "intercept", date = date)
        val morningVerse = useCase(slot = "morning", date = date)

        assertTrue("intercept" in interceptVerse.slots)
        assertTrue("morning" in morningVerse.slots)
    }

    @Test
    fun differentDays_returnValidVerseFromPool() {
        val pool = interceptVerses
        for (day in 0L..29L) {
            val date = LocalDate.of(2024, 1, 1).plusDays(day)
            val verse = useCase(slot = "intercept", date = date)
            assertNotNull(verse)
            assertTrue("verse not from intercept pool", pool.contains(verse))
        }
    }

    @Test
    fun allSlotsReturnFromCorrectPool() {
        val date = LocalDate.of(2024, 6, 15)
        val interceptResult = useCase(slot = "intercept", date = date)
        val morningResult = useCase(slot = "morning", date = date)

        assertTrue(interceptVerses.contains(interceptResult))
        assertTrue(morningVerses.contains(morningResult))
    }

    @Test
    fun seenIds_excludedWhenPoolLargeEnough() {
        val date = LocalDate.of(2024, 3, 1)
        val first = useCase(slot = "intercept", date = date)
        val second = useCase(slot = "intercept", seenIds = setOf(first.id), date = date)
        assertNotNull(second)
        assertTrue("second verse should differ when first is excluded", second.id != first.id)
    }

    @Test
    fun seenIds_ignoredWhenFreshPoolTooSmall() {
        val date = LocalDate.of(2024, 4, 1)
        val firstVerse = useCase(slot = "intercept", date = date)
        val almostAllSeen = (interceptVerses.map { it.id }.toSet() - firstVerse.id).take(8).toSet()
        val result = useCase(slot = "intercept", seenIds = almostAllSeen, date = date)
        assertNotNull(result)
        assertTrue(interceptVerses.contains(result))
    }

    @Test
    fun tone_grounding_filtersCorrectly() {
        val groundingVerses = (1..5).map { i ->
            Verse("g$i", "G $i:$i", "Grounding text $i", listOf("intercept"), listOf("grounding"))
        }
        val invitingVerses = (1..5).map { i ->
            Verse("inv$i", "Inv $i:$i", "Inviting text $i", listOf("intercept"), listOf("inviting"))
        }
        val repo = VerseRepository { groundingVerses + invitingVerses }
        val uc = GetVerseForSlot(repo)

        val result = uc(slot = "intercept", tone = "grounding", date = LocalDate.of(2024, 5, 1))
        assertTrue("grounding" in result.tone)
    }

    @Test
    fun tone_defaultsToInvitingForIntercept() {
        val groundingVerses = (1..5).map { i ->
            Verse("g$i", "G $i:$i", "Grounding $i", listOf("intercept"), listOf("grounding"))
        }
        val invitingVerses = (1..5).map { i ->
            Verse("inv$i", "Inv $i:$i", "Inviting $i", listOf("intercept"), listOf("inviting"))
        }
        val repo = VerseRepository { groundingVerses + invitingVerses }
        val uc = GetVerseForSlot(repo)

        val result = uc(slot = "intercept", date = LocalDate.of(2024, 5, 2))
        assertTrue("inviting" in result.tone)
    }
}
