package com.maneo.app.feature.journal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun JournalNavHost() {
    var showEntry by remember { mutableStateOf(false) }

    if (showEntry) {
        JournalEntryScreen(onBack = { showEntry = false })
    } else {
        JournalListScreen(onNewEntry = { showEntry = true })
    }
}
