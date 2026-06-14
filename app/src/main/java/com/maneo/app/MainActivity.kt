package com.maneo.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.maneo.app.core.util.NotificationHelper
import com.maneo.app.ui.navigation.AppNavHost
import com.maneo.app.ui.navigation.Routes
import com.maneo.app.ui.theme.ManeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.setPendingSlot(intent.getStringExtra(NotificationHelper.EXTRA_SLOT))
        enableEdgeToEdge()
        setContent {
            ManeoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val onboardingDone by mainViewModel.onboardingDone.collectAsState()
                    val pendingSlot by mainViewModel.pendingSlot.collectAsState()

                    when (onboardingDone) {
                        null -> Box(Modifier.fillMaxSize()) // DataStore loading — one frame
                        false -> AppNavHost(startDestination = Routes.ONBOARDING_WELCOME)
                        true -> AppNavHost(
                            startDestination = Routes.HOME,
                            pendingSlot = pendingSlot,
                            onSlotConsumed = mainViewModel::clearPendingSlot,
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        mainViewModel.setPendingSlot(intent.getStringExtra(NotificationHelper.EXTRA_SLOT))
    }
}
