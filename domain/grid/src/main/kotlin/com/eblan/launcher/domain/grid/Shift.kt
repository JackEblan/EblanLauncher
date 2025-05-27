package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemShift
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWithShift(
    gridItems: MutableList<GridItem>,
    oldGridItem: GridItem,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    val gridItemShift = getGridItemShift(
        oldGridItem = oldGridItem,
        movingGridItem = movingGridItem,
    )

    return if (shiftConflicts(
            gridItems = gridItems,
            gridItemShift = gridItemShift,
            movingGridItem = movingGridItem,
            rows = rows,
            columns = columns,
        )
    ) {
        gridItems
    } else {
        null
    }
}

private suspend fun shiftConflicts(
    gridItems: MutableList<GridItem>,
    gridItemShift: GridItemShift,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in gridItems) {
        if (!coroutineContext.isActive) return false

        // Skip the moving grid item itself.
        if (gridItem.id == movingGridItem.id) continue

        if (rectanglesOverlap(movingGridItem, gridItem)) {
            val shiftedItem = shiftItem(
                gridItemShift = gridItemShift,
                movingGridItem = movingGridItem,
                conflictingGridItem = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            // Update the grid with the shifted item.
            val index = gridItems.indexOfFirst { it.id == gridItem.id }

            gridItems[index] = shiftedItem

            // Recursively resolve further conflicts from the shifted item.
            if (!shiftConflicts(
                    gridItems = gridItems,
                    gridItemShift = gridItemShift,
                    movingGridItem = shiftedItem,
                    rows = rows,
                    columns = columns,
                )
            ) {
                return false
            }
        }
    }

    return true
}

private fun shiftItem(
    gridItemShift: GridItemShift,
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    return when (gridItemShift) {
        GridItemShift.Up, GridItemShift.Left -> {
            shiftItemToLeft(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
                rows = rows,
                columns = columns,
            )
        }

        GridItemShift.Down, GridItemShift.Right -> {
            shiftItemToRight(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
                rows = rows,
                columns = columns,
            )
        }
    }
}

private fun shiftItemToRight(
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn + movingGridItem.columnSpan
    var newRow = conflictingGridItem.startRow

    // Wrap horizontally if necessary.
    if (newColumn + conflictingGridItem.columnSpan > columns) {
        newColumn = 0
        newRow = movingGridItem.startRow + movingGridItem.rowSpan
    }

    // Check vertical bounds.
    if (newRow + conflictingGridItem.rowSpan > rows) {
        return null // No space left.
    }

    return conflictingGridItem.copy(startRow = newRow, startColumn = newColumn)
}

private fun shiftItemToLeft(
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn - conflictingGridItem.columnSpan
    var newRow = conflictingGridItem.startRow

    // Wrap horizontally if necessary
    if (newColumn < 0) {
        newColumn = columns - conflictingGridItem.columnSpan
        newRow = conflictingGridItem.startRow - 1
    }

    // Check vertical bounds
    if (newRow < 0) {
        return null // No space left
    }

    if (newRow + conflictingGridItem.rowSpan > rows) {
        return null
    }

    return conflictingGridItem.copy(startRow = newRow, startColumn = newColumn)
}

private fun getGridItemShift(
    oldGridItem: GridItem,
    movingGridItem: GridItem,
): GridItemShift {
    val oldCenterRow = oldGridItem.startRow + oldGridItem.rowSpan / 2.0
    val oldCenterColumn = oldGridItem.startColumn + oldGridItem.columnSpan / 2.0

    val newCenterRow = movingGridItem.startRow + movingGridItem.rowSpan / 2.0
    val newCenterColumn = movingGridItem.startColumn + movingGridItem.columnSpan / 2.0

    val rowDiff = newCenterRow - oldCenterRow
    val columnDiff = newCenterColumn - oldCenterColumn

    return when {
        rowDiff < 0 -> GridItemShift.Up
        rowDiff > 0 -> GridItemShift.Down
        columnDiff < 0 -> GridItemShift.Left
        columnDiff > 0 -> GridItemShift.Right
        else -> GridItemShift.Right
    }
}