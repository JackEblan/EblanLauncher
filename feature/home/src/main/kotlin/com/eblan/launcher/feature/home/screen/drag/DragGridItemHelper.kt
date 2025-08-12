package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
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
    gridItemSource: GridItemSource,
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

        val moveGridItem = getMoveGridItem(
            targetPage = targetPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            rows = dockRows,
            columns = dockColumns,
            gridWidth = rootWidth,
            gridHeight = dockHeight,
            gridX = dragIntOffset.x,
            gridY = dockY,
            associate = Associate.Dock,
            gridItemSource = gridItemSource,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = moveGridItem,
            rows = dockRows,
            columns = dockColumns,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveGridItem(
                moveGridItem,
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

        val moveGridItem = getMoveGridItem(
            targetPage = targetPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            rows = rows,
            columns = columns,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            gridX = gridX,
            gridY = gridY,
            associate = Associate.Grid,
            gridItemSource = gridItemSource,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = moveGridItem,
            rows = rows,
            columns = columns,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveGridItem(
                moveGridItem,
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

private fun getMoveGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
    gridItemSource: GridItemSource,
): GridItem {
    return when (gridItemSource) {
        is GridItemSource.Existing -> {
            getMoveExistingGridItem(
                targetPage = targetPage,
                gridItem = gridItem,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                gridX = gridX,
                gridY = gridY,
                associate = associate,
            )
        }

        is GridItemSource.New, is GridItemSource.Pin -> {
            getMoveNewGridItem(
                targetPage = targetPage,
                gridItem = gridItem,
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                gridX = gridX,
                gridY = gridY,
                associate = associate,
            )
        }
    }
}

private fun getMoveNewGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellHeight: Int,
    cellWidth: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
): GridItem {
    return when (val data = gridItem.data) {
        is GridItemData.Widget -> {
            val (checkedRowSpan, checkedColumnSpan) = getWidgetGridItemSpan(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                minHeight = data.minHeight,
                minWidth = data.minWidth,
                targetCellHeight = data.targetCellHeight,
                targetCellWidth = data.targetCellWidth,
            )

            val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
                rows = rows,
                columns = columns,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                minWidth = data.minWidth,
                minHeight = data.minHeight,
                targetCellWidth = data.targetCellWidth,
                targetCellHeight = data.targetCellHeight,
            )

            val newData = data.copy(
                minWidth = checkedMinWidth,
                minHeight = checkedMinHeight,
            )

            gridItem.copy(
                page = targetPage,
                startRow = gridY / cellHeight,
                startColumn = gridX / cellWidth,
                rowSpan = checkedRowSpan,
                columnSpan = checkedColumnSpan,
                associate = associate,
                data = newData,
            )
        }

        else -> {
            gridItem.copy(
                page = targetPage,
                startRow = gridY / cellHeight,
                startColumn = gridX / cellWidth,
                associate = associate,
            )
        }
    }
}

private fun getMoveExistingGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
) = when (gridItem.data) {
    is GridItemData.Widget -> {
        gridItem.copy(
            page = targetPage,
            startRow = gridY / cellHeight,
            startColumn = gridX / cellWidth,
            associate = associate,
        )
    }

    else -> {
        gridItem.copy(
            page = targetPage,
            startRow = gridY / cellHeight,
            startColumn = gridX / cellWidth,
            associate = associate,
        )
    }
}