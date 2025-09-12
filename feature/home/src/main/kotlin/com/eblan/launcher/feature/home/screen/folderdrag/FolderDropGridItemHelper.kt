package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

suspend fun handleFolderGridItemOnDragEnd(
    density: Density,
    currentPage: Int,
    dragIntOffset: IntOffset,
    screenHeight: Int,
    gridPadding: Int,
    pageIndicatorHeight: Int,
    paddingValues: PaddingValues,
    onDragEnd: (Int) -> Unit,
    onDragCancel: () -> Unit,
) {
    delay(200L)

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val verticalPadding = topPadding + bottomPadding

    val gridHeight = screenHeight - verticalPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnTopGrid = dragY < gridPadding

    val isOnBottomGrid = dragY > gridHeight - pageIndicatorHeight - gridPadding

    val isVerticalBounds = !isOnTopGrid && !isOnBottomGrid

    if (isVerticalBounds) {
        onDragEnd(currentPage)
    } else {
        onDragCancel()
    }
}