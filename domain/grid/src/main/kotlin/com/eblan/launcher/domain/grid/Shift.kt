package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

fun resolveConflictsWithShift(
    gridItems: MutableList<GridItem>,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (shiftConflicts(
            gridItems = gridItems,
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
    gridItems: MutableList<GridItem>,
    movingItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in gridItems) {
        // Skip the moving grid item itself.
        if (gridItem.id == movingItem.id) continue

        if (rectanglesOverlap(movingItem, gridItem)) {
            val shiftedItem = shiftItemToRight(
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
                    gridItems = gridItems,
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