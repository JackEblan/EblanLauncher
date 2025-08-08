package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag

fun handleFolderDragIntOffset(
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    columns: Int,
    rows: Int,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    if (drag != Drag.Dragging) {
        return
    }

    val gridWidth = rootWidth - (gridPadding * 2)

    val gridHeight = rootHeight - (gridPadding * 2)

    val gridX = dragIntOffset.x - gridPadding

    val gridY = dragIntOffset.y - gridPadding

    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val newGridItem = gridItem.copy(
        page = gridItem.page,
        startRow = gridY / cellHeight,
        startColumn = gridX / cellWidth,
        associate = Associate.Grid,
    )

    val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
        gridItem = newGridItem,
        rows = rows,
        columns = columns,
    )

    if (isGridItemSpanWithinBounds) {
        onMoveFolderGridItem(
            newGridItem,
            gridX,
            gridY,
            rows,
            columns,
            gridWidth,
            gridHeight,
        )
    }
}