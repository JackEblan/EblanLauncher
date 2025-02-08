package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

fun pixelsToGridCells(
    newPixelWidth: Int,
    newPixelHeight: Int,
    gridCellPixelWidth: Int,
    gridCellPixelHeight: Int,
): Pair<Int, Int> {
    val newWidth = (newPixelWidth / gridCellPixelWidth).coerceAtLeast(1)
    val newHeight = (newPixelHeight / gridCellPixelHeight).coerceAtLeast(1)
    return Pair(newWidth, newHeight)
}

fun resizeGridItemWithPixels(
    gridItem: GridItem?,
    newPixelWidth: Int,
    newPixelHeight: Int,
    gridCellPixelWidth: Int,
    gridCellPixelHeight: Int,
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
    oldCells: List<GridCell>, newWidth: Int, newHeight: Int
): List<GridCell> {
    val topLeftCell = oldCells.minWith(compareBy({ it.row }, { it.column }))

    val newCells = mutableListOf<GridCell>()
    for (row in topLeftCell.row until topLeftCell.row + newHeight) {
        for (col in topLeftCell.column until topLeftCell.column + newWidth) {
            newCells.add(GridCell(row, col))
        }
    }
    return newCells
}

fun resizeGridItem(
    gridItem: GridItem?,
    newWidth: Int,
    newHeight: Int,
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateResizedCells(gridItem.cells, newWidth, newHeight)
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}