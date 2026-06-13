package com.maneo.app.core.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

private val Context.testDataStore by preferencesDataStore(name = "maneo_prefs_test")

@RunWith(AndroidJUnit4::class)
class DataStoreTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun teardown() {
        // Remove test prefs file so tests don't bleed into each other
        context.filesDir.resolve("datastore/maneo_prefs_test.preferences_pb").delete()
    }

    @Test
    fun onboardingDone_defaultsToNull_thenWriteAndRead() = runTest {
        val initial = context.testDataStore.data.first()[PrefsKeys.ONBOARDING_DONE]
        assertNull(initial)

        context.testDataStore.edit { it[PrefsKeys.ONBOARDING_DONE] = true }

        val after = context.testDataStore.data.first()[PrefsKeys.ONBOARDING_DONE]
        assertEquals(true, after)
    }

    @Test
    fun screenTimeThreshold_writeAndRead() = runTest {
        context.testDataStore.edit { it[PrefsKeys.SCREEN_TIME_THRESHOLD_MINS] = 45 }
        val value = context.testDataStore.data.first()[PrefsKeys.SCREEN_TIME_THRESHOLD_MINS]
        assertEquals(45, value)
    }

    @Test
    fun blockedApps_writeAndRead() = runTest {
        val apps = setOf("com.instagram.android", "com.zhiliaoapp.musically")
        context.testDataStore.edit { it[PrefsKeys.BLOCKED_APPS] = apps }
        val value = context.testDataStore.data.first()[PrefsKeys.BLOCKED_APPS]
        assertEquals(apps, value)
    }

    @Test
    fun reminderSlot_writeAndRead() = runTest {
        context.testDataStore.edit {
            it[PrefsKeys.MORNING_TIME]    = "07:00"
            it[PrefsKeys.MORNING_ENABLED] = true
        }
        val prefs = context.testDataStore.data.first()
        assertEquals("07:00", prefs[PrefsKeys.MORNING_TIME])
        assertEquals(true,    prefs[PrefsKeys.MORNING_ENABLED])
    }
}
