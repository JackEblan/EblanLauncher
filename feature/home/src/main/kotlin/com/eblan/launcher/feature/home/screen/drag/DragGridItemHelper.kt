package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

suspend fun handlePageDirection(
    currentPage: Int,
    pageDirection: PageDirection?,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    when (pageDirection) {
        PageDirection.Left -> {
            onAnimateScrollToPage(currentPage - 1)
        }

        PageDirection.Right -> {
            onAnimateScrollToPage(currentPage + 1)
        }

        null -> Unit
    }
}

suspend fun handleDragIntOffset(
    targetPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    dockHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    dockColumns: Int,
    dockRows: Int,
    rows: Int,
    columns: Int,
    isScrollInProgress: Boolean,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    if (drag != Drag.Dragging || isScrollInProgress) {
        return
    }

    val isDraggingOnDock = dragIntOffset.y > (rootHeight - dockHeight) - gridPadding

    if (dragIntOffset.x <= gridPadding && !isDraggingOnDock) {
        delay(250L)

        onUpdatePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
        delay(250L)

        onUpdatePageDirection(PageDirection.Right)
    } else if (isDraggingOnDock) {
        val cellWidth = rootWidth / dockColumns

        val cellHeight = dockHeight / dockRows

        val dockY = dragIntOffset.y - (rootHeight - dockHeight)

        val newGridItem = gridItem.copy(
            page = targetPage,
            startRow = dockY / cellHeight,
            startColumn = dragIntOffset.x / cellWidth,
            associate = Associate.Dock,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = newGridItem,
            rows = dockRows,
            columns = dockColumns,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveGridItem(
                newGridItem,
                dragIntOffset.x,
                dockY,
                dockRows,
                dockColumns,
                rootWidth,
                dockHeight,
            )
        }
    } else {
        val gridWidth = rootWidth - (gridPadding * 2)

        val gridHeight = (rootHeight - dockHeight) - (gridPadding * 2)

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
            onMoveGridItem(
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