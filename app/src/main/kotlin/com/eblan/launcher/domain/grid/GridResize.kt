package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

fun pixelsToGridCells(
    newPixelWidth: Int, // New width in pixels
    newPixelHeight: Int, // New height in pixels
    gridCellPixelWidth: Int, // Width of a single grid cell in pixels
    gridCellPixelHeight: Int // Height of a single grid cell in pixels
): Pair<Int, Int> {
    val newWidth = (newPixelWidth / gridCellPixelWidth).coerceAtLeast(1) // Ensure at least 1 cell
    val newHeight =
        (newPixelHeight / gridCellPixelHeight).coerceAtLeast(1) // Ensure at least 1 cell
    return Pair(newWidth, newHeight)
}

fun resizeGridItemWithPixels(
    gridItem: GridItem?, // The list of grid items
    newPixelWidth: Int, // New width in pixels
    newPixelHeight: Int, // New height in pixels
    gridCellPixelWidth: Int, // Width of a single grid cell in pixels
    gridCellPixelHeight: Int, // Height of a single grid cell in pixels
): GridItem? {
    val (newWidth, newHeight) = pixelsToGridCells(
        newPixelWidth = newPixelWidth,
        newPixelHeight = newPixelHeight,
        gridCellPixelWidth = gridCellPixelWidth,
        gridCellPixelHeight = gridCellPixelHeight
    )

    return resizeGridItem(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
    )
}

fun calculateResizedCells(
    oldCells: List<GridCell>, // Current cells of the grid item
    newWidth: Int, // New width in terms of grid cells
    newHeight: Int // New height in terms of grid cells
): List<GridCell> {
    // Find the top-left cell of the grid item
    val topLeftCell = oldCells.minWith(compareBy({ it.row }, { it.column }))

    // Calculate the new cells
    val newCells = mutableListOf<GridCell>()
    for (row in topLeftCell.row until topLeftCell.row + newHeight) {
        for (col in topLeftCell.column until topLeftCell.column + newWidth) {
            newCells.add(GridCell(row, col))
        }
    }
    return newCells
}

fun resizeGridItem(
    gridItem: GridItem?, // The list of grid items
    newWidth: Int, // New width in terms of grid cells
    newHeight: Int, // New height in terms of grid
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateResizedCells(gridItem.cells, newWidth, newHeight)
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}