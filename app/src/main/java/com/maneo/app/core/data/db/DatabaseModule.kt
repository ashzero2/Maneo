package com.maneo.app.core.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ManeoDatabase =
        Room.databaseBuilder(context, ManeoDatabase::class.java, "maneo.db").build()

    @Provides
    fun provideJournalDao(db: ManeoDatabase): JournalDao = db.journalDao()
}
