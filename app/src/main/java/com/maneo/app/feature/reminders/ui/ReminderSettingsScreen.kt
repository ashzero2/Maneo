package com.maneo.app.feature.reminders.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maneo.app.feature.reminders.repository.ReminderSettings

@Composable
fun ReminderSettingsScreen(
    viewModel: ReminderSettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()
    var showPickerFor by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Daily Reminders",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "A quiet moment with God, delivered to you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        ReminderRow(
            label = "Morning",
            displayTime = settings.morningTime.toDisplayTime(),
            enabled = settings.morningEnabled,
            onToggle = { viewModel.setEnabled("morning", it) },
            onTimeTap = { showPickerFor = "morning" },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ReminderRow(
            label = "Afternoon",
            displayTime = settings.afternoonTime.toDisplayTime(),
            enabled = settings.afternoonEnabled,
            onToggle = { viewModel.setEnabled("afternoon", it) },
            onTimeTap = { showPickerFor = "afternoon" },
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ReminderRow(
            label = "Evening",
            displayTime = settings.eveningTime.toDisplayTime(),
            enabled = settings.eveningEnabled,
            onToggle = { viewModel.setEnabled("evening", it) },
            onTimeTap = { showPickerFor = "evening" },
        )
    }

    showPickerFor?.let { slot ->
        val time = slot.currentTime(settings)
        val (h, m) = time.split(":").map { it.toInt() }
        SlotTimePicker(
            initialHour = h,
            initialMinute = m,
            onDismiss = { showPickerFor = null },
            onConfirm = { hour, minute ->
                viewModel.setTime(slot, hour, minute)
                showPickerFor = null
            },
        )
    }
}

@Composable
private fun ReminderRow(
    label: String,
    displayTime: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeTap: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onTimeTap) {
            Text(
                text = displayTime,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SlotTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = { TimePicker(state = state) },
    )
}

private fun String.toDisplayTime(): String {
    val (h, m) = split(":").map { it.toInt() }
    val amPm = if (h < 12) "AM" else "PM"
    val displayH = when {
        h == 0 -> 12
        h > 12 -> h - 12
        else -> h
    }
    return "%d:%02d %s".format(displayH, m, amPm)
}

private fun String.currentTime(settings: ReminderSettings): String = when (this) {
    "morning" -> settings.morningTime
    "afternoon" -> settings.afternoonTime
    else -> settings.eveningTime
}
