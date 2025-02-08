package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

fun getGridItemDimensions(cells: List<GridCell>): Pair<Int, Int> {
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
    gridItem: GridItem, rows: Int, columns: Int
): Boolean {
    for (cell in gridItem.cells) {
        if (cell.row < 0 || cell.row >= rows || cell.column < 0 || cell.column >= columns) {
            return false
        }
    }
    return true
}

fun calculateBoundingBox(
    gridCells: List<GridCell>, rows: Int, columns: Int, screenWidth: Int, screenHeight: Int
): BoundingBox {
    val cellWidth = screenWidth / rows
    val cellHeight = screenHeight / columns

    val minRow = gridCells.minOf { it.row }
    val maxRow = gridCells.maxOf { it.row }
    val minCol = gridCells.minOf { it.column }
    val maxCol = gridCells.maxOf { it.column }

    val width = (maxCol - minCol + 1) * cellWidth
    val height = (maxRow - minRow + 1) * cellHeight

    return BoundingBox(width = width, height = height)
}


fun calculateCoordinates(
    gridCells: List<GridCell>, rows: Int, columns: Int, screenWidth: Int, screenHeight: Int
): Coordinates {
    val cellWidth = screenWidth / rows
    val cellHeight = screenHeight / columns

    val minRow = gridCells.minOf { it.row }
    val minCol = gridCells.minOf { it.column }

    val x = minCol * cellWidth
    val y = minRow * cellHeight

    return Coordinates(x = x, y = y)
}

