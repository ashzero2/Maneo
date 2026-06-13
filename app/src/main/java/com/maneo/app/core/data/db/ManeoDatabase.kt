package com.maneo.app.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [JournalEntryEntity::class], version = 1, exportSchema = false)
abstract class ManeoDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}
