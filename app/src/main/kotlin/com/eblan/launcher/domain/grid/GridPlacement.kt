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

fun isGridItemWithinBounds(
    gridItem: GridItem, gridRows: Int, gridCols: Int
): Boolean {
    for (cell in gridItem.cells) {
        if (cell.row < 0 || cell.row >= gridRows || cell.column < 0 || cell.column >= gridCols) {
            return false
        }
    }
    return true
}

