package com.maneo.app.feature.journal.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun JournalNavHost(initialSlot: String? = null) {
    var entrySlot by remember { mutableStateOf(initialSlot) }
    var showEntry by remember { mutableStateOf(initialSlot != null) }

    if (showEntry) {
        JournalEntryScreen(
            slot = entrySlot,
            onBack = {
                showEntry = false
                entrySlot = null
            },
        )
    } else {
        JournalListScreen(
            onNewEntry = {
                entrySlot = null
                showEntry = true
            },
        )
    }
}
