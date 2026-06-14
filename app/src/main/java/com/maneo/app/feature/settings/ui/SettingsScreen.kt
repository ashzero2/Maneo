package com.maneo.app.feature.settings.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt

private const val GITHUB_URL = "https://github.com/maneo-app/maneo"
private val THRESHOLD_VALUES = listOf(15, 30, 60, 90, 120)

@Composable
fun SettingsScreen(
    onNavigateToReminders: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToLicences: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val thresholdMins by viewModel.thresholdMins.collectAsState()
    val context = LocalContext.current

    val savedIndex = THRESHOLD_VALUES.indexOf(thresholdMins).coerceAtLeast(0)
    var sliderPos by remember(savedIndex) { mutableFloatStateOf(savedIndex.toFloat()) }
    val displayMins = THRESHOLD_VALUES[sliderPos.roundToInt().coerceIn(0, THRESHOLD_VALUES.lastIndex)]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(32.dp))

        // Screen time threshold
        Text(
            text = "Screen time",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Remind me after $displayMins minutes on a blocked app",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Slider(
            value = sliderPos,
            onValueChange = { sliderPos = it },
            onValueChangeFinished = {
                viewModel.setThreshold(
                    THRESHOLD_VALUES[sliderPos.roundToInt().coerceIn(0, THRESHOLD_VALUES.lastIndex)]
                )
            },
            valueRange = 0f..4f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            THRESHOLD_VALUES.forEach { value ->
                Text(
                    text = "${value}m",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        SettingsRow(label = "Reminders", onClick = onNavigateToReminders)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        SettingsRow(label = "Blocked apps", onClick = onNavigateToApps)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Spacer(Modifier.weight(1f))

        // About
        Text(
            text = "Maneo is free and open source.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "GitHub",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            },
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Open source licences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onNavigateToLicences() },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Built with love, for peace.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
