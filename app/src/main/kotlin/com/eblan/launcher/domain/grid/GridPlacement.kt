package com.eblan.launcher.domain.grid

import com.eblan.launcher.GridCell
import com.eblan.launcher.GridItem

fun getItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
    if (cells.isEmpty()) return 0 to 0
    val minRow = cells.minOf { it.row }
    val maxRow = cells.maxOf { it.row }
    val minCol = cells.minOf { it.column }
    val maxCol = cells.maxOf { it.column }
    val requiredRows = maxRow - minRow + 1
    val requiredCols = maxCol - minCol + 1
    return requiredRows to requiredCols
}

fun canGridItemFitAnywhere(
    movingGridItem: GridItem,
    existingGridItems: List<GridItem>,
    page: Int,
    gridRows: Int,
    gridCols: Int
): Boolean {
    val grid = Array(gridRows) { BooleanArray(gridCols) }

    // 1. Mark cells occupied by *existing* grid items on the same page
    existingGridItems.filter { it.page == page && it.id != movingGridItem.id }
        .forEach { existingItem ->
            existingItem.cells.forEach { cell ->
                if (cell.row in 0 until gridRows && cell.column in 0 until gridCols) {
                    grid[cell.row][cell.column] = true // Mark as occupied
                }
            }
        }

    val (requiredRows, requiredCols) = getItemDimensions(movingGridItem.cells)

    // 2. Iterate through all possible top-left positions for the movingGridItem
    for (startRow in 0..gridRows - requiredRows) { // Possible starting rows
        for (startCol in 0..gridCols - requiredCols) { // Possible starting columns
            var canPlaceHere = true // Assume we can place at this position initially

            // 3. Check for overlaps at this potential position
            for (rowOffset in 0 until requiredRows) {
                for (colOffset in 0 until requiredCols) {
                    val checkRow = startRow + rowOffset
                    val checkCol = startCol + colOffset
                    if (grid[checkRow][checkCol]) { // Cell is already occupied
                        canPlaceHere = false // Cannot place here due to overlap
                        break // No need to check other cells for this position
                    }
                }
                if (!canPlaceHere) break // Move to next starting column if overlap found
            }

            if (canPlaceHere) {
                return true // Found a position where it fits! Return true immediately.
            }
        }
    }

    return false // No position found where the movingGridItem can fit without overlap
}

fun isGridItemWithinBounds(
    gridItem: GridItem,
    gridRows: Int,
    gridCols: Int
): Boolean {
    for (cell in gridItem.cells) {
        if (cell.row < 0 || cell.row >= gridRows || cell.column < 0 || cell.column >= gridCols) {
            return false
        }
    }
    return true
}