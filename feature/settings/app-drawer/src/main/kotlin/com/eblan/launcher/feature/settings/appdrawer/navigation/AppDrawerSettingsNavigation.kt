package com.eblan.launcher.feature.settings.appdrawer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eblan.launcher.feature.settings.appdrawer.AppDrawerSettingsRoute

fun NavController.navigateToAppDrawerSettings() {
    navigate(AppDrawerSettingsRouteData)
}

fun NavGraphBuilder.appDrawerSettingsScreen(
    onNavigateUp: () -> Unit,
) {
    composable<AppDrawerSettingsRouteData> {
        AppDrawerSettingsRoute(onNavigateUp = onNavigateUp)
    }
}