package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemShift
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

fun CoroutineScope.resolveConflictsWithShift(
    gridItems: MutableList<GridItem>,
    gridItemShift: GridItemShift?,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (shiftConflicts(
            isActive = isActive,
            gridItems = gridItems,
            gridItemShift = gridItemShift,
            movingItem = movingGridItem,
            rows = rows,
            columns = columns,
        )
    ) {
        gridItems
    } else {
        null
    }
}

private fun shiftConflicts(
    isActive: Boolean,
    gridItems: MutableList<GridItem>,
    gridItemShift: GridItemShift?,
    movingItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in gridItems) {
        if (isActive.not()) return false

        // Skip the moving grid item itself.
        if (gridItem.id == movingItem.id) continue

        if (rectanglesOverlap(movingItem, gridItem)) {
            val shiftedItem = shiftItem(
                gridItemShift = gridItemShift,
                movingGridItem = movingItem,
                other = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            // Update the grid with the shifted item.
            val index = gridItems.indexOfFirst { it.id == gridItem.id }
            gridItems[index] = shiftedItem

            // Recursively resolve further conflicts from the shifted item.
            if (!shiftConflicts(
                    isActive = isActive,
                    gridItems = gridItems,
                    gridItemShift = gridItemShift,
                    movingItem = shiftedItem,
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
    gridItemShift: GridItemShift?,
    movingGridItem: GridItem,
    other: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    return when (gridItemShift) {
        GridItemShift.Up, GridItemShift.Left -> {
            shiftItemToLeft(
                movingGridItem = movingGridItem,
                other = other,
                rows = rows,
                columns = columns,
            )
        }

        GridItemShift.Down, GridItemShift.Right -> {
            shiftItemToRight(
                movingGridItem = movingGridItem,
                other = other,
                rows = rows,
                columns = columns,
            )
        }

        null -> {
            null
        }
    }
}

private fun shiftItemToRight(
    movingGridItem: GridItem,
    other: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn + movingGridItem.columnSpan
    var newRow = other.startRow

    // Wrap horizontally if necessary.
    if (newColumn + other.columnSpan > columns) {
        newColumn = 0
        newRow = movingGridItem.startRow + movingGridItem.rowSpan
    }

    // Check vertical bounds.
    if (newRow + other.rowSpan > rows) {
        return null // No space left.
    }
    return other.copy(startRow = newRow, startColumn = newColumn)
}

private fun shiftItemToLeft(
    movingGridItem: GridItem,
    other: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn - other.columnSpan
    var newRow = other.startRow

    if (newColumn < 0) {
        newColumn = columns - other.columnSpan
        newRow = other.startRow - 1
    }

    if (newRow < 0) {
        return null
    }

    if (newRow + other.rowSpan > rows) {
        return null
    }

    return other.copy(startRow = newRow, startColumn = newColumn)
}