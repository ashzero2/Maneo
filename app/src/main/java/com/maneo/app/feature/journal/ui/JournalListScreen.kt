package com.maneo.app.feature.journal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maneo.app.core.domain.model.JournalEntry
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun JournalListScreen(
    onNewEntry: () -> Unit,
    viewModel: JournalListViewModel = hiltViewModel(),
) {
    val entries by viewModel.entries.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewEntry,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "New entry",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Your prayers live here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryRow(entry)
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryRow(entry: JournalEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = entry.createdAt.toRelativeDate(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = entry.text.take(120),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 2,
        )
    }
}

private fun Long.toRelativeDate(): String {
    val then = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    val today = LocalDate.now()
    return when (then.toLocalDate()) {
        today -> then.format(DateTimeFormatter.ofPattern("h:mm a"))
        today.minusDays(1) -> "Yesterday"
        else -> if (then.year == today.year)
            then.format(DateTimeFormatter.ofPattern("MMM d"))
        else
            then.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    }
}
