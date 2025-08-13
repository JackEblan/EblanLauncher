package com.eblan.launcher.feature.edit.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.eblan.launcher.feature.edit.EditRoute

fun NavController.navigateToEditScreen(id: String) {
    navigate(EditRouteData(id = id))
}

fun NavGraphBuilder.editScreen(
    onNavigateUp: () -> Unit,
) {
    composable<EditRouteData> {
        EditRoute(onNavigateUp = onNavigateUp)
    }
}