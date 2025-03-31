package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

fun resolveConflictsWithShift(
    gridItems: MutableList<GridItem>,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (shiftConflicts(gridItems, movingGridItem, rows, columns)) gridItems else null
}

/**
 * Shifts the [other] grid item based on the reference [movingGridItem]'s right edge.
 * The new start column of [other] is set to (movingGridItem.startColumn + movingGridItem.columnSpan).
 * If that placement exceeds the grid width, it wraps by resetting the column to 0 and moving down one row.
 * Returns null if the new position would be out of the grid's bounds.
 */
private fun shiftItem(
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

/**
 * Recursively resolves conflicts in the [gridItems].
 * For every grid item that overlaps with [movingItem] (except the moving grid item itself),
 * shift it using [shiftItem] (using [movingItem] as the reference) and update the grid.
 * Then, recursively process any conflicts that arise from the shifted item.
 */
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
            val shiftedItem = shiftItem(
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