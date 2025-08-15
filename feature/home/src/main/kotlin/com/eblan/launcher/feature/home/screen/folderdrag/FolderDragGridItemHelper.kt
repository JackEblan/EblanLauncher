package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

suspend fun handleFolderDragIntOffset(
    targetPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onMoveOutsideFolder: (GridItemSource) -> Unit,
) {
    if (drag != Drag.Dragging || isScrollInProgress) {
        return
    }

    val verticalOutOfBounds =
        dragIntOffset.y < gridPadding || dragIntOffset.y > rootHeight - gridPadding

    if (dragIntOffset.x <= gridPadding && !verticalOutOfBounds) {
        delay(250L)

        onUpdatePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !verticalOutOfBounds) {
        delay(250L)

        onUpdatePageDirection(PageDirection.Right)
    } else if (verticalOutOfBounds) {
        onMoveOutsideFolder(
            GridItemSource.Existing(gridItem = gridItem.copy(folderId = null)),
        )
    } else {
        val gridWidth = rootWidth - (gridPadding * 2)

        val gridHeight = rootHeight - (gridPadding * 2)

        val gridX = dragIntOffset.x - gridPadding

        val gridY = dragIntOffset.y - gridPadding

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val newGridItem = gridItem.copy(
            page = targetPage,
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
}