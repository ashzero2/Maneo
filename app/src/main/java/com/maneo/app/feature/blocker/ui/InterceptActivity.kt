package com.maneo.app.feature.blocker.ui

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.maneo.app.ui.theme.ManeoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InterceptActivity : ComponentActivity() {

    private val viewModel: InterceptViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show over lock screen — use code path for API 27+, flags for API 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        setContent {
            ManeoTheme {
                InterceptScreen(
                    verse = viewModel.verse,
                    prayer = viewModel.prayer,
                    onAmen = { finish() },
                )
            }
        }
    }
}
