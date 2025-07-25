package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.delay

fun onDroppedExisting(
    drag: Drag,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDragEnd: (Int) -> Unit,
    onDragCancel: () -> Unit,
) {
    if (drag == Drag.End) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        onDragEnd(targetPage)

        return
    }

    if (drag == Drag.Cancel) {
        onDragCancel()
    }
}

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
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    gridItems: Map<Int, List<GridItem>>,
    dockGridItems: List<GridItem>,
    drag: Drag,
    gridItem: GridItem?,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    dockHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    dockColumns: Int,
    dockRows: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveGridItem: (
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    if (drag != Drag.Dragging ||
        gridItem == null ||
        isScrollInProgress
    ) {
        return
    }

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val gridItemsByPage = gridItems[targetPage].orEmpty()

    val isDraggingOnDock =
        dragIntOffset.y > (rootHeight - dockHeight) - gridPadding

    val delay = 500L

    if (dragIntOffset.x <= gridPadding && !isDraggingOnDock) {
        delay(delay)

        onUpdatePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
        delay(delay)

        onUpdatePageDirection(PageDirection.Right)
    } else if (isDraggingOnDock) {
        delay(delay)

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
                dockGridItems,
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
        delay(delay)

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
                gridItemsByPage,
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