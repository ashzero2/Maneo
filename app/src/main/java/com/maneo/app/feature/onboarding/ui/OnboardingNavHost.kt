package com.maneo.app.feature.onboarding.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

private sealed class OnboardingDest {
    object Welcome : OnboardingDest()
    object Permissions : OnboardingDest()
    object FirstBlock : OnboardingDest()
}

@Composable
fun OnboardingNavHost(
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    var dest by remember { mutableStateOf<OnboardingDest>(OnboardingDest.Welcome) }

    when (dest) {
        is OnboardingDest.Welcome -> WelcomeScreen(
            onGetStarted = { dest = OnboardingDest.Permissions },
        )
        is OnboardingDest.Permissions -> PermissionsScreen(
            onContinue = { dest = OnboardingDest.FirstBlock },
        )
        is OnboardingDest.FirstBlock -> FirstBlockScreen(
            onComplete = { viewModel.complete() },
        )
    }
}
