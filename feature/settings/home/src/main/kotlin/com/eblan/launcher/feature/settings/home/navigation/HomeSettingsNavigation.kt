package com.eblan.launcher.feature.settings.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eblan.launcher.feature.settings.home.HomeSettingsRoute

fun NavController.navigateToHomeSettings() {
    navigate(HomeSettingsRouteData)
}

fun NavGraphBuilder.homeSettingsScreen(
    onNavigateUp: () -> Unit,
) {
    composable<HomeSettingsRouteData> {
        HomeSettingsRoute(onNavigateUp = onNavigateUp)
    }
}