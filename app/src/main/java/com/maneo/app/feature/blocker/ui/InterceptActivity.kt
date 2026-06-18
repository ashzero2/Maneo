package com.maneo.app.feature.blocker.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                val verse by viewModel.verse.collectAsState()
                val prayer by viewModel.prayer.collectAsState()
                val timerEnabled by viewModel.timerEnabled.collectAsState()
                val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsState()
                val remainingSeconds by viewModel.remainingSeconds.collectAsState()
                val isSabbath by viewModel.isSabbath.collectAsState()
                if (verse != null && prayer != null) {
                    InterceptScreen(
                        verse = verse!!,
                        prayer = prayer!!,
                        timerEnabled = timerEnabled,
                        remainingSeconds = remainingSeconds,
                        timerTotalSeconds = timerTotalSeconds,
                        isSabbath = isSabbath,
                        onWait = {
                            viewModel.recordWait()
                            startActivity(
                                Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_HOME)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                            finish()
                        },
                        onContinue = { viewModel.recordContinue(); finish() },
                    )
                }
            }
        }
    }
}
