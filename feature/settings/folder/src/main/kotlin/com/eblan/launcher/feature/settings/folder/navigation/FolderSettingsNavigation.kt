package com.eblan.launcher.feature.settings.folder.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eblan.launcher.feature.settings.folder.FolderSettingsRoute

fun NavController.navigateToFolderSettings() {
    navigate(FolderSettingsRouteData)
}

fun NavGraphBuilder.folderSettingsScreen(
    onNavigateUp: () -> Unit,
) {
    composable<FolderSettingsRouteData> {
        FolderSettingsRoute(onNavigateUp = onNavigateUp)
    }
}