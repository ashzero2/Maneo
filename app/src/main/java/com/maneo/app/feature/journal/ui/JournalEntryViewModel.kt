package com.maneo.app.feature.journal.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.core.data.prefs.PrefsKeys
import com.maneo.app.feature.journal.domain.SaveEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class JournalEntryViewModel @Inject constructor(
    private val saveEntry: SaveEntry,
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var prompt by mutableStateOf<String?>(null)
        private set

    var text by mutableStateOf("")
        private set

    var saved by mutableStateOf(false)
        private set

    private var activeSlot: String? = null
    private var configured = false

    suspend fun configure(slot: String?) {
        if (configured) return
        configured = true
        activeSlot = slot

        if (slot == "evening") {
            val prefs = dataStore.data.first()
            val storedDay = prefs[PrefsKeys.TODAY_INTERCEPT_DATE] ?: 0L
            val todayCount = if (LocalDate.now().toEpochDay() == storedDay) {
                prefs[PrefsKeys.TODAY_INTERCEPT_COUNT] ?: 0
            } else 0
            if (todayCount >= EVENING_SUGGESTION_THRESHOLD) {
                prompt = "You paused $todayCount times today. What were you looking for?"
                return
            }
        }

        prompt = loadPromptForSlot(slot)
    }

    fun dismissPrompt() { prompt = null }

    fun onTextChange(value: String) { text = value }

    fun save() {
        if (text.isBlank()) return
        viewModelScope.launch {
            saveEntry(text, promptUsed = prompt, slot = activeSlot)
            saved = true
        }
    }

    private fun loadPromptForSlot(slot: String?): String? = try {
        val raw = context.assets.open("prompts.json").bufferedReader().readText()
        val obj = Json.parseToJsonElement(raw).jsonObject
        val key = if (slot in setOf("morning", "afternoon", "evening")) slot else "general"
        val arr = obj[key]?.jsonArray?.map { it.jsonPrimitive.content } ?: return null
        arr[abs(LocalDate.now().toEpochDay().toInt()) % arr.size]
    } catch (_: Exception) {
        null
    }

    private companion object {
        const val EVENING_SUGGESTION_THRESHOLD = 5
    }
}
