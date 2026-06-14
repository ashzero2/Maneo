package com.maneo.app.feature.reminders.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maneo.app.feature.reminders.repository.ReminderRepository
import com.maneo.app.feature.reminders.repository.ReminderSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderSettingsViewModel @Inject constructor(
    private val repository: ReminderRepository,
) : ViewModel() {

    val settings: StateFlow<ReminderSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReminderSettings.DEFAULT)

    fun setEnabled(slot: String, enabled: Boolean) {
        viewModelScope.launch {
            val s = settings.value
            val time = when (slot) {
                "morning" -> s.morningTime
                "afternoon" -> s.afternoonTime
                else -> s.eveningTime
            }
            repository.setSlot(slot, enabled, time)
        }
    }

    fun setTime(slot: String, hour: Int, minute: Int) {
        viewModelScope.launch {
            val s = settings.value
            val enabled = when (slot) {
                "morning" -> s.morningEnabled
                "afternoon" -> s.afternoonEnabled
                else -> s.eveningEnabled
            }
            repository.setSlot(slot, enabled, "%02d:%02d".format(hour, minute))
        }
    }
}
