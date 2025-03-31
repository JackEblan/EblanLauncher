package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

fun resolveConflictsWithShift(
    gridItems: List<GridItem>,
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    // Create a mutable copy of the grid; if the moving grid item exists, update it; otherwise add it.
    val grid = gridItems.toMutableList().apply {
        val index = indexOfFirst { it.id == movingGridItem.id }
        if (index != -1) {
            set(index, movingGridItem)
        } else {
            add(movingGridItem)
        }
    }
    return if (shiftConflicts(grid, movingGridItem, rows, columns)) grid else null
}

/**
 * Shifts the [other] grid item based on the reference [movingGridItem]'s right edge.
 * The new start column of [other] is set to (movingGridItem.startColumn + movingGridItem.columnSpan).
 * If that placement exceeds the grid width, it wraps by resetting the column to 0 and moving down one row.
 * Returns null if the new position would be out of the grid's bounds.
 */
fun shiftItem(movingGridItem: GridItem, other: GridItem, rows: Int, columns: Int): GridItem? {
    var newColumn = movingGridItem.startColumn + movingGridItem.columnSpan
    var newRow = movingGridItem.startRow

    // Wrap horizontally if necessary.
    if (newColumn + other.columnSpan > columns) {
        newColumn = 0
        newRow += 1
    }
    // Check vertical bounds.
    if (newRow + other.rowSpan > rows) {
        return null // No space left.
    }
    return other.copy(startRow = newRow, startColumn = newColumn)
}

/**
 * Recursively resolves conflicts in the [grid].
 * For every grid item that overlaps with [movingItem] (except the moving grid item itself),
 * shift it using [shiftItem] (using [movingItem] as the reference) and update the grid.
 * Then, recursively process any conflicts that arise from the shifted item.
 */
fun shiftConflicts(
    grid: MutableList<GridItem>,
    movingItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in grid) {
        // Skip the moving grid item itself.
        if (gridItem.id == movingItem.id) continue

        if (rectanglesOverlap(movingItem, gridItem)) {
            val shiftedItem = shiftItem(movingItem, gridItem, rows, columns) ?: return false

            // Update the grid with the shifted item.
            val index = grid.indexOfFirst { it.id == gridItem.id }
            grid[index] = shiftedItem

            // Recursively resolve further conflicts from the shifted item.
            if (!shiftConflicts(grid, shiftedItem, rows, columns)) {
                return false
            }
        }
    }
    return true
}