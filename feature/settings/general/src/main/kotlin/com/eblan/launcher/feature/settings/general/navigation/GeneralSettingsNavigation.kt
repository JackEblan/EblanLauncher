package com.eblan.launcher.feature.settings.general.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eblan.launcher.feature.settings.general.GeneralSettingsRoute

fun NavController.navigateToGeneralSettings() {
    navigate(GeneralSettingsRouteData)
}

fun NavGraphBuilder.generalSettingsScreen(
    onNavigateUp: () -> Unit,
) {
    composable<GeneralSettingsRouteData> {
        GeneralSettingsRoute(onNavigateUp = onNavigateUp)
    }
}