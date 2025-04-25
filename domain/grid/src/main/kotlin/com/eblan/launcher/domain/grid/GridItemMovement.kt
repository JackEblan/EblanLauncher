package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

/**
 * Moves the specified grid item to a new position based on screen coordinates and the dimensions of its bounding box.
 *
 * Converts the provided pixel coordinates ([x], [y]) into a target starting position (startRow and startColumn)
 * and then updates the grid item accordingly.
 *
 * @param gridItem The [GridItem] to be moved.
 * @param x The x-coordinate (in pixels) where the grid item is to be moved.
 * @param y The y-coordinate (in pixels) where the grid item is to be moved.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @param gridWidth The width of the screen (or container) in pixels.
 * @param gridHeight The height of the screen (or container) in pixels.
 * @return A new [GridItem] with updated starting position.
 */
fun moveGridItemWithCoordinates(
    gridItem: GridItem,
    x: Int,
    y: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
): GridItem {
    val (startRow, startColumn) = coordinatesToStartPosition(
        x = x,
        y = y,
        rows = rows,
        columns = columns,
        gridWidth = gridWidth,
        gridHeight = gridHeight,
    )

    return gridItem.copy(
        startRow = startRow,
        startColumn = startColumn,
    )
}
