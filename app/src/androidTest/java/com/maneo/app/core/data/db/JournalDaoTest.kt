package com.maneo.app.core.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalDaoTest {

    private lateinit var db: ManeoDatabase
    private lateinit var dao: JournalDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ManeoDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.journalDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertEntry_returnsPositiveId() = runTest {
        val entry = JournalEntryEntity(
            text       = "Lord, thank you for today.",
            promptUsed = "What are you carrying into this day?",
            slot       = "morning",
            createdAt  = 1_000_000L,
        )
        val id = dao.insert(entry)
        assertTrue(id > 0)
    }

    @Test
    fun insertAndQuery_returnsCorrectEntry() = runTest {
        val entry = JournalEntryEntity(
            text       = "Be still and know.",
            promptUsed = null,
            slot       = null,
            createdAt  = 2_000_000L,
        )
        dao.insert(entry)
        val entries = dao.getAll().first()
        assertEquals(1, entries.size)
        assertEquals(entry.text, entries[0].text)
        assertEquals(entry.promptUsed, entries[0].promptUsed)
        assertEquals(entry.slot, entries[0].slot)
    }

    @Test
    fun multipleEntries_returnedNewestFirst() = runTest {
        dao.insert(JournalEntryEntity(text = "First",  promptUsed = null, slot = null, createdAt = 1_000L))
        dao.insert(JournalEntryEntity(text = "Second", promptUsed = null, slot = null, createdAt = 2_000L))
        dao.insert(JournalEntryEntity(text = "Third",  promptUsed = null, slot = null, createdAt = 3_000L))

        val entries = dao.getAll().first()
        assertEquals(3, entries.size)
        assertEquals("Third",  entries[0].text)
        assertEquals("Second", entries[1].text)
        assertEquals("First",  entries[2].text)
    }

    @Test
    fun mapper_toDomain_roundtrip() = runTest {
        val entity = JournalEntryEntity(
            text       = "Rest in him.",
            promptUsed = "Evening prompt",
            slot       = "evening",
            createdAt  = 5_000L,
        )
        val id = dao.insert(entity)
        val fromDb = dao.getAll().first().first()
        val domain = fromDb.toDomain()

        assertEquals(id, domain.id)
        assertEquals(entity.text, domain.text)
        assertEquals(entity.promptUsed, domain.promptUsed)
        assertEquals(entity.slot, domain.slot)
        assertEquals(entity.createdAt, domain.createdAt)

        // Round-trip back to entity
        val backToEntity = domain.toEntity()
        assertEquals(domain.text, backToEntity.text)
    }
}
