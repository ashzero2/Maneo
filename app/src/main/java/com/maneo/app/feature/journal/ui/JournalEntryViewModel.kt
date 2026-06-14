package com.maneo.app.feature.journal.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.feature.journal.domain.SaveEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @ApplicationContext context: Context,
) : ViewModel() {

    val prompt: String? = loadGeneralPrompt(context)

    var text by mutableStateOf("")
        private set

    var saved by mutableStateOf(false)
        private set

    fun onTextChange(value: String) { text = value }

    fun save(slot: String? = null) {
        if (text.isBlank()) return
        viewModelScope.launch {
            saveEntry(text, promptUsed = if (text.isNotBlank()) prompt else null, slot = slot)
            saved = true
        }
    }

    private fun loadGeneralPrompt(context: Context): String? = try {
        val raw = context.assets.open("prompts.json").bufferedReader().readText()
        val arr = Json.parseToJsonElement(raw).jsonObject["general"]?.jsonArray
            ?.map { it.jsonPrimitive.content } ?: return null
        val index = abs(LocalDate.now().toEpochDay().toInt()) % arr.size
        arr[index]
    } catch (_: Exception) {
        null
    }
}
