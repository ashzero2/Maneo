package com.maneo.app.feature.verse.repository

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.maneo.app.core.domain.model.Verse
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private val verseJson = Json { ignoreUnknownKeys = true }

private val fallbackVerses = listOf(
    Verse(
        id = "fallback",
        reference = "Matthew 11:28",
        text = "Come to me, all you who labor and are heavily burdened, and I will give you rest.",
        slots = listOf("intercept", "morning", "afternoon", "evening"),
        tone = listOf("inviting"),
    )
)

@Module
@InstallIn(SingletonComponent::class)
object VerseModule {

    @Provides
    @Singleton
    fun provideVerseRepository(@ApplicationContext context: Context): VerseRepository =
        VerseRepository {
            try {
                val raw = context.assets.open("verses.json").bufferedReader().readText()
                verseJson.decodeFromString(raw)
            } catch (_: Exception) {
                fallbackVerses
            }
        }
}
