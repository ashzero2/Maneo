package com.maneo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.feature.journal.ui.JournalNavHost
import com.maneo.app.feature.reminders.ui.ReminderSettingsScreen
import com.maneo.app.ui.theme.ManeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private var pendingSlot: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingSlot = intent.getStringExtra(NotificationHelper.EXTRA_SLOT)
        enableEdgeToEdge()
        render()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingSlot = intent.getStringExtra(NotificationHelper.EXTRA_SLOT)
        render()
    }

    private fun render() {
        val slot = pendingSlot
        setContent {
            ManeoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (slot != null) {
                        // Opened from a reminder notification — go straight to journal entry
                        JournalNavHost(initialSlot = slot)
                    } else {
                        // Temporary: replaced by nav shell in Phase 9
                        ReminderSettingsScreen()
                    }
                }
            }
        }
    }
}
