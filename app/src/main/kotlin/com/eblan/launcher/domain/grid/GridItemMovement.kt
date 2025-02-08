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
    x: Int,
    y: Int,
    rows: Int,
    columns: Int,
    screenWidth: Int,
    screenHeight: Int,
    boundingBoxWidth: Int,
    boundingBoxHeight: Int,
): GridItem? {
    val targetCell = coordinatesToGridCell(
        x = x,
        y = y,
        rows = rows,
        columns = columns,
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
    x: Int,
    y: Int,
    rows: Int,
    columns: Int,
    screenWidth: Int,
    screenHeight: Int,
    boundingBoxWidth: Int,
    boundingBoxHeight: Int
): GridCell {
    val cellWidth = screenWidth / rows
    val cellHeight = screenHeight / columns

    val centerX = x + boundingBoxWidth / 2
    val centerY = y + boundingBoxHeight / 2

    val row = (centerY / cellHeight).coerceIn(0 until columns)
    val column = (centerX / cellWidth).coerceIn(0 until rows)

    return GridCell(row, column)
}

fun calculateNewCells(oldCells: List<GridCell>, targetCell: GridCell): List<GridCell> {
    val rowOffset = targetCell.row - oldCells[0].row
    val colOffset = targetCell.column - oldCells[0].column
    return oldCells.map { cell ->
        GridCell(cell.row + rowOffset, cell.column + colOffset)
    }
}