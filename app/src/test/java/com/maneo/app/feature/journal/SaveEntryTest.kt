package com.maneo.app.feature.journal

import com.maneo.app.core.data.db.JournalDao
import com.maneo.app.core.data.db.JournalEntryEntity
import com.maneo.app.feature.journal.domain.SaveEntry
import com.maneo.app.feature.journal.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SaveEntryTest {

    private val inserted = mutableListOf<JournalEntryEntity>()

    private val fakeDao = object : JournalDao {
        override fun getAll(): Flow<List<JournalEntryEntity>> = flowOf(emptyList())
        override suspend fun insert(entry: JournalEntryEntity): Long {
            inserted.add(entry)
            return inserted.size.toLong()
        }
    }

    private val saveEntry = SaveEntry(JournalRepository(fakeDao))

    @Test
    fun blankText_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { saveEntry("   ", null, null) }
        }
    }

    @Test
    fun emptyText_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            runTest { saveEntry("", null, null) }
        }
    }

    @Test
    fun validText_isTrimmedAndSaved() = runTest {
        saveEntry("  Lord, help me.  ", promptUsed = "A prompt", slot = "morning")
        assertEquals(1, inserted.size)
        assertEquals("Lord, help me.", inserted[0].text)
        assertEquals("A prompt", inserted[0].promptUsed)
        assertEquals("morning", inserted[0].slot)
    }

    @Test
    fun savedEntry_hasCreatedAtSet() = runTest {
        val before = System.currentTimeMillis()
        saveEntry("My prayer", null, null)
        val after = System.currentTimeMillis()
        assert(inserted[0].createdAt in before..after)
    }
}
