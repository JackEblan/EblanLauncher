package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

/**
 * Moves the specified grid item to a new position based on screen coordinates and the dimensions of its bounding box.
 *
 * This function converts the provided pixel coordinates ([x], [y]) along with the bounding box dimensions
 * ([boundingBoxWidth], [boundingBoxHeight]) into a target grid cell. It then moves the grid item so that its
 * relative cell positions are shifted accordingly.
 *
 * @param gridItem The [GridItem] to be moved. If `null`, the function returns `null`.
 * @param x The x-coordinate (in pixels) where the grid item is to be moved.
 * @param y The y-coordinate (in pixels) where the grid item is to be moved.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param screenWidth The width of the screen (or container) in pixels.
 * @param screenHeight The height of the screen (or container) in pixels.
 * @param boundingBoxWidth The width of the grid item's bounding box in pixels.
 * @param boundingBoxHeight The height of the grid item's bounding box in pixels.
 * @return A new [GridItem] with updated cell positions if [gridItem] is not `null`; otherwise, `null`.
 */
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
    val targetCell = coordinatesToGridCellWithBoundingBox(
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

/**
 * Moves the given grid item so that its first cell is relocated to the specified target cell.
 *
 * The movement is achieved by calculating an offset based on the difference between the target cell and
 * the grid item's current first cell. This offset is then applied uniformly to all cells of the grid item.
 *
 * @param gridItem The [GridItem] to move. If `null`, the function returns `null`.
 * @param targetCell The target [GridCell] to which the grid item's first cell should be aligned.
 * @return A new [GridItem] with repositioned cells if [gridItem] is not `null`; otherwise, `null`.
 */
private fun moveGridItem(
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

/**
 * Converts pixel coordinates into a grid cell while taking into account the dimensions of a bounding box.
 *
 * This function first calculates the center point of the bounding box using the provided top‑left
 * coordinates ([x], [y]) and the bounding box dimensions ([boundingBoxWidth], [boundingBoxHeight]). It then
 * determines the grid cell corresponding to that center point based on the screen dimensions and the grid's
 * configuration.
 *
 * **Note:** The computed row index is clamped to the range `0 until columns` and the column index to `0 until rows`.
 * This clamping might appear reversed; ensure that this behavior aligns with your grid coordinate system.
 *
 * @param x The x-coordinate (in pixels) of the top‑left corner of the bounding box.
 * @param y The y-coordinate (in pixels) of the top‑left corner of the bounding box.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param screenWidth The width of the screen (or container) in pixels.
 * @param screenHeight The height of the screen (or container) in pixels.
 * @param boundingBoxWidth The width of the bounding box in pixels.
 * @param boundingBoxHeight The height of the bounding box in pixels.
 * @return A [GridCell] corresponding to the center of the bounding box, converted into grid coordinates.
 */
private fun coordinatesToGridCellWithBoundingBox(
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

    // Calculate the center of the bounding box.
    val centerX = x + boundingBoxWidth / 2
    val centerY = y + boundingBoxHeight / 2

    // Convert the center point into grid coordinates and clamp them within valid ranges.
    val row = (centerY / cellHeight).coerceIn(0 until columns)
    val column = (centerX / cellWidth).coerceIn(0 until rows)

    return GridCell(row, column)
}

/**
 * Calculates new grid cell positions for a grid item by shifting its cells so that the first cell aligns with
 * the [targetCell]. The relative positioning of the cells is maintained.
 *
 * The shift (or offset) is computed as the difference between the [targetCell] and the first cell in [oldCells].
 * This offset is then applied to every cell in [oldCells] to determine the new positions.
 *
 * @param oldCells A list of [GridCell] objects representing the current positions of the grid item.
 * @param targetCell The [GridCell] representing the desired new position for the grid item's first cell.
 * @return A list of [GridCell] objects representing the grid item's new cell positions.
 */
private fun calculateNewCells(oldCells: List<GridCell>, targetCell: GridCell): List<GridCell> {
    val rowOffset = targetCell.row - oldCells[0].row
    val colOffset = targetCell.column - oldCells[0].column
    return oldCells.map { cell ->
        GridCell(cell.row + rowOffset, cell.column + colOffset)
    }
}
