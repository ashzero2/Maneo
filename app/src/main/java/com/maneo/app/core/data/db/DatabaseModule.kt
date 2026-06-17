package com.maneo.app.core.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// To enable SQLCipher at-rest encryption:
//   1. Add to build.gradle: implementation("net.zetetic:android-database-sqlcipher:4.5.4")
//   2. Import: net.sqlcipher.database.SupportFactory
//   3. Uncomment the SupportFactory lines below

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ManeoDatabase {
        val passphrase = DatabaseKeyProvider.getKey(context)
        // val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, ManeoDatabase::class.java, "maneo.db")
            // .openHelperFactory(factory)
            .build()
    }

    @Provides
    fun provideJournalDao(db: ManeoDatabase): JournalDao = db.journalDao()
}
