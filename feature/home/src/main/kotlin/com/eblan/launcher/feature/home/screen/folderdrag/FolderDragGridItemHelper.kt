package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

suspend fun handleFolderDragIntOffset(
    density: Density,
    targetPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    gridHeight: Int,
    gridPadding: Int,
    gridWidth: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    paddingValues: PaddingValues,
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

    delay(250L)

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnLeftGrid = dragX < gridPadding

    val isOnRightGrid = dragX > gridWidth - gridPadding

    val isOnTopGrid = dragY < gridPadding

    val isOnBottomGrid = dragY > gridHeight - gridPadding

    val isHorizontalBounds = !isOnLeftGrid && !isOnRightGrid

    val isVerticalBounds = !isOnTopGrid && !isOnBottomGrid

    if (isOnLeftGrid && isVerticalBounds) {
        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid && isVerticalBounds) {
        delay(250L)

        onUpdatePageDirection(PageDirection.Right)
    } else if (!isVerticalBounds) {
        onMoveOutsideFolder(
            GridItemSource.Existing(gridItem = gridItem.copy(folderId = null)),
        )
    } else {
        val gridWidthWithPadding = gridWidth - (gridPadding * 2)

        val gridHeightWithPadding = gridHeight - (gridPadding * 2)

        val gridX = dragX - gridPadding

        val gridY = dragY - gridPadding

        val cellWidth = gridWidthWithPadding / columns

        val cellHeight = gridHeightWithPadding / rows

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
                gridWidthWithPadding,
                gridHeightWithPadding,
            )
        }
    }
}