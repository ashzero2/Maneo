package com.maneo.app.feature.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Licence(val name: String, val copyright: String, val licence: String)

private val LICENCES = listOf(
    Licence("Jetpack Compose", "The Android Open Source Project", "Apache License 2.0"),
    Licence("Dagger / Hilt", "Google Inc.", "Apache License 2.0"),
    Licence("AndroidX Room", "The Android Open Source Project", "Apache License 2.0"),
    Licence("AndroidX DataStore", "The Android Open Source Project", "Apache License 2.0"),
    Licence("AndroidX WorkManager", "The Android Open Source Project", "Apache License 2.0"),
    Licence("AndroidX Navigation", "The Android Open Source Project", "Apache License 2.0"),
    Licence("kotlinx.serialization", "JetBrains s.r.o.", "Apache License 2.0"),
    Licence("kotlinx.coroutines", "JetBrains s.r.o.", "Apache License 2.0"),
    Licence("Lora", "Cyreal", "SIL Open Font License 1.1"),
    Licence("Inter", "Rasmus Andersson", "SIL Open Font License 1.1"),
)

@Composable
fun OssLicencesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))
        Text(
            text = "Open source licences",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))

        LICENCES.forEachIndexed { index, item ->
            LicenceRow(item)
            if (index < LICENCES.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            }
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun LicenceRow(item: Licence) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = item.copyright,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = item.licence,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
