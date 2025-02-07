package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

fun moveGridItem(
    gridItem: GridItem?,
    targetCell: GridCell,
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateNewCells(gridItem.cells, targetCell)
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}

fun moveGridItemWithCoordinates(
    gridItem: GridItem?,
    x: Int, // X-coordinate in pixels
    y: Int, // Y-coordinate in pixels
    gridWidth: Int, // Number of columns in the grid
    gridHeight: Int, // Number of rows in the grid
    screenWidth: Int, // Total width of the screen
    screenHeight: Int, // Total height of the screen
    boundingBoxWidth: Int, // Width of the item in pixels
    boundingBoxHeight: Int,
): GridItem? {
    val targetCell = coordinatesToGridCell(
        x = x,
        y = y,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        boundingBoxWidth = boundingBoxWidth,
        boundingBoxHeight = boundingBoxHeight
    )

    return moveGridItem(
        gridItem = gridItem, targetCell = targetCell
    )
}

fun coordinatesToGridCell(
    x: Int, // X-coordinate in pixels
    y: Int, // Y-coordinate in pixels
    gridWidth: Int, // Number of columns in the grid
    gridHeight: Int, // Number of rows in the grid
    screenWidth: Int, // Total width of the screen
    screenHeight: Int, // Total height of the screen
    boundingBoxWidth: Int, // Width of the item in pixels
    boundingBoxHeight: Int // Height of the item in pixels
): GridCell {
    val cellWidth = screenWidth / gridWidth
    val cellHeight = screenHeight / gridHeight

    val centerX = x + boundingBoxWidth / 2
    val centerY = y + boundingBoxHeight / 2

    val row = (centerY / cellHeight).coerceIn(0 until gridHeight)
    val column = (centerX / cellWidth).coerceIn(0 until gridWidth)

    return GridCell(row, column)
}

fun calculateNewCells(oldCells: List<GridCell>, targetCell: GridCell): List<GridCell> {
    val rowOffset = targetCell.row - oldCells[0].row
    val colOffset = targetCell.column - oldCells[0].column
    return oldCells.map { cell ->
        GridCell(cell.row + rowOffset, cell.column + colOffset)
    }
}