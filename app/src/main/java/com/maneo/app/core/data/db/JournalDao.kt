package com.maneo.app.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY createdAt DESC")
    fun getAll(): Flow<List<JournalEntryEntity>>

    @Insert
    suspend fun insert(entry: JournalEntryEntity): Long
}
