package com.maneo.app.feature.blocker.repository

import android.content.Context
import com.maneo.app.feature.blocker.domain.Prayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

private val prayerJson = Json { ignoreUnknownKeys = true }

private val fallbackPrayers = listOf(
    Prayer(id = "fallback", text = "Lord, I give you this moment. Let it be yours.")
)

@Module
@InstallIn(SingletonComponent::class)
object PrayerModule {

    @Provides
    @Singleton
    fun providePrayerRepository(@ApplicationContext context: Context): PrayerRepository =
        PrayerRepository {
            try {
                val raw = context.assets.open("prayers.json").bufferedReader().readText()
                prayerJson.decodeFromString(raw)
            } catch (_: Exception) {
                fallbackPrayers
            }
        }
}
