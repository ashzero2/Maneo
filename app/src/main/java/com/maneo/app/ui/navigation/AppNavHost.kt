package com.maneo.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.maneo.app.feature.blocker.ui.AppSelectorScreen
import com.maneo.app.feature.home.ui.HomeScreen
import com.maneo.app.feature.journal.ui.JournalEntryScreen
import com.maneo.app.feature.journal.ui.JournalListScreen
import com.maneo.app.feature.onboarding.ui.FirstBlockScreen
import com.maneo.app.feature.onboarding.ui.OnboardingViewModel
import com.maneo.app.feature.onboarding.ui.PermissionsScreen
import com.maneo.app.feature.onboarding.ui.WelcomeScreen
import com.maneo.app.feature.reminders.ui.ReminderSettingsScreen
import com.maneo.app.feature.settings.ui.SettingsScreen

@Composable
fun AppNavHost(
    startDestination: String,
    pendingSlot: String? = null,
    onSlotConsumed: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
) {
    val currentRoute by navController.currentBackStackEntryAsState()
    val route = currentRoute?.destination?.route
    val showBottomBar = route in setOf(Routes.HOME, Routes.JOURNAL_LIST, Routes.SETTINGS)

    LaunchedEffect(pendingSlot) {
        if (pendingSlot != null) {
            navController.navigate("journal/entry?slot=$pendingSlot") {
                popUpTo(Routes.HOME) { inclusive = false }
            }
            onSlotConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                    )
                    NavigationBarItem(
                        selected = route == Routes.JOURNAL_LIST,
                        onClick = {
                            navController.navigate(Routes.JOURNAL_LIST) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Journal") },
                        label = { Text("Journal") },
                    )
                    NavigationBarItem(
                        selected = route == Routes.SETTINGS,
                        onClick = {
                            navController.navigate(Routes.SETTINGS) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier,
        ) {
            // Onboarding
            composable(Routes.ONBOARDING_WELCOME) {
                WelcomeScreen(onGetStarted = { navController.navigate(Routes.ONBOARDING_PERMISSIONS) })
            }
            composable(Routes.ONBOARDING_PERMISSIONS) {
                PermissionsScreen(onContinue = { navController.navigate(Routes.ONBOARDING_FIRST_BLOCK) })
            }
            composable(Routes.ONBOARDING_FIRST_BLOCK) {
                val onboardingVm: OnboardingViewModel = hiltViewModel()
                FirstBlockScreen(onComplete = { onboardingVm.complete() })
            }

            // Home
            composable(Routes.HOME) {
                HomeScreen(
                    onWritePrayer = { navController.navigate("journal/entry") },
                    onViewApps = { navController.navigate(Routes.APP_SELECTOR) },
                )
            }

            // Journal
            composable(Routes.JOURNAL_LIST) {
                JournalListScreen(onNewEntry = { navController.navigate("journal/entry") })
            }
            composable(
                route = Routes.JOURNAL_ENTRY,
                arguments = listOf(
                    navArgument("slot") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                JournalEntryScreen(
                    slot = backStackEntry.arguments?.getString("slot"),
                    onBack = { navController.popBackStack() },
                )
            }

            // Settings and sub-screens
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateToReminders = { navController.navigate(Routes.REMINDERS_SETTINGS) },
                    onNavigateToApps = { navController.navigate(Routes.APP_SELECTOR) },
                )
            }
            composable(Routes.REMINDERS_SETTINGS) { ReminderSettingsScreen() }
            composable(Routes.APP_SELECTOR) { AppSelectorScreen() }
        }
    }
}
