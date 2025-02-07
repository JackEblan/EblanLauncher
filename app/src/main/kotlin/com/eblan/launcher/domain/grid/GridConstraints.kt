package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.BoundingBox
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
    gridItem: GridItem, gridRows: Int, gridCols: Int
): Boolean {
    for (cell in gridItem.cells) {
        if (cell.row < 0 || cell.row >= gridRows || cell.column < 0 || cell.column >= gridCols) {
            return false
        }
    }
    return true
}

// Function to calculate the bounding box for a list of grid cells
fun calculateBoundingBox(
    gridCells: List<GridCell>, // List of grid cells the item occupies
    gridWidth: Int,            // Number of columns in the grid
    gridHeight: Int,           // Number of rows in the grid
    screenWidth: Int,          // Total width of the screen
    screenHeight: Int          // Total height of the screen
): BoundingBox {
    // Calculate cell dimensions
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    // Find the minimum and maximum row and column indices
    val minRow = gridCells.minOf { it.row }
    val maxRow = gridCells.maxOf { it.row }
    val minCol = gridCells.minOf { it.column }
    val maxCol = gridCells.maxOf { it.column }

    // Calculate the total width and height
    val width = (maxCol - minCol + 1) * cellWidth
    val height = (maxRow - minRow + 1) * cellHeight

    return BoundingBox(width, height)
}

data class Coordinates(val x: Int, val y: Int)

fun calculateCoordinates(
    gridCells: List<GridCell>, // List of grid cells the item occupies
    gridWidth: Int,            // Number of columns in the grid
    gridHeight: Int,           // Number of rows in the grid
    screenWidth: Int,          // Total width of the screen
    screenHeight: Int
): Coordinates {
    // Calculate cell dimensions
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    // Find the minimum and maximum row and column indices
    val minRow = gridCells.minOf { it.row }
    val minCol = gridCells.minOf { it.column }

    // Calculate the top-left corner (x, y)
    val x = minCol * cellWidth
    val y = minRow * cellHeight

    return Coordinates(x, y)
}

