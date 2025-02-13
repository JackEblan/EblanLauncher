package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.EdgeState
import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

/**
 * Computes the dimensions (number of rows and columns) required to fully enclose the provided grid cells.
 *
 * The dimensions are determined by finding the minimum and maximum row and column indices among the cells.
 * The number of required rows is calculated as (maxRow - minRow + 1) and the number of required columns as
 * (maxCol - minCol + 1).
 *
 * @param cells A list of [GridCell] objects representing the occupied positions of a grid item.
 * @return A [Pair] where the first value is the number of required rows and the second value is the number of required columns.
 *         If [cells] is empty, the function returns `0 to 0`.
 */
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

/**
 * Determines whether all cells of a given grid item lie within the bounds of a grid.
 *
 * The grid is assumed to have the specified number of [rows] and [columns]. Each cell of the [gridItem]
 * is checked to ensure its row index is within `0 until rows` and its column index is within `0 until columns`.
 *
 * @param gridItem The [GridItem] whose cells are to be checked.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @return `true` if every cell of [gridItem] is within the grid bounds; `false` otherwise.
 */
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

/**
 * Calculates the bounding box for a set of grid cells in pixel dimensions.
 *
 * The function computes the width and height of the bounding box based on the cell dimensions derived from
 * the provided screen size and grid configuration. Note that the cell dimensions are computed as:
 * - `cellWidth = screenWidth / rows`
 * - `cellHeight = screenHeight / columns`
 *
 * Then, using the minimum and maximum row and column indices from [gridCells], it calculates:
 * - `width = (maxCol - minCol + 1) * cellWidth`
 * - `height = (maxRow - minRow + 1) * cellHeight`
 *
 * @param gridCells A list of [GridCell] objects for which the bounding box is calculated.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param screenWidth The total width of the screen (or container) in pixels.
 * @param screenHeight The total height of the screen (or container) in pixels.
 * @return A [BoundingBox] object containing the computed width and height.
 */
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

/**
 * Calculates the pixel coordinates of the top-left corner for a given set of grid cells.
 *
 * The function determines the cell dimensions using the provided [screenWidth] and [screenHeight] along with
 * the grid configuration ([rows] and [columns]). It then computes the coordinates based on the minimum row and
 * column indices among [gridCells]:
 * - `x = minCol * cellWidth`
 * - `y = minRow * cellHeight`
 *
 * @param gridCells A list of [GridCell] objects for which the coordinates are determined.
 * @param rows The number of rows in the grid.
 * @param columns The number of columns in the grid.
 * @param screenWidth The width of the screen (or container) in pixels.
 * @param screenHeight The height of the screen (or container) in pixels.
 * @return A [Coordinates] object representing the top-left pixel position of the grid cells.
 */
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

/**
 * Validates that all provided grid cells have row and column indices within the acceptable grid bounds.
 *
 * Each cell in [gridCells] is verified to have a row index in the range `0 until rows` and a column index
 * in the range `0 until columns`.
 *
 * @param gridCells A list of [GridCell] objects to validate.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @return `true` if all cells are within bounds; `false` if any cell lies outside the valid grid range.
 */
fun areValidCells(gridCells: List<GridCell>, rows: Int, columns: Int): Boolean {
    return gridCells.all { it.row in 0 until rows && it.column in 0 until columns }
}

/**
 * Converts pixel coordinates into the corresponding grid cell based on the grid's configuration and screen size.
 *
 * The conversion is done by first calculating the cell dimensions:
 * - `cellWidth = screenWidth / rows`
 * - `cellHeight = screenHeight / columns`
 *
 * Then, the row and column are determined by dividing the pixel coordinates by the cell dimensions.
 * The resulting indices are coerced into the valid range (`0 until rows` and `0 until columns`) to ensure
 * that the returned [GridCell] is within the grid bounds.
 *
 * @param x The x-coordinate (in pixels).
 * @param y The y-coordinate (in pixels).
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @param screenWidth The width of the screen (or container) in pixels.
 * @param screenHeight The height of the screen (or container) in pixels.
 * @return A [GridCell] corresponding to the position of the provided coordinates on the grid.
 */
fun coordinatesToGridCell(
    x: Int, y: Int, rows: Int, columns: Int, screenWidth: Int, screenHeight: Int
): GridCell {
    val cellWidth = screenWidth / rows
    val cellHeight = screenHeight / columns

    val row = (y / cellHeight).coerceIn(0 until rows)
    val column = (x / cellWidth).coerceIn(0 until columns)

    return GridCell(row, column)
}

/**
 * Determines the edge state of a grid item based on its horizontal position and dimensions.
 *
 * @param x The x-coordinate of the grid item's position.
 * @param boundingBoxWidth The width of the grid item's bounding box.
 * @param screenWidth The total width of the screen or container.
 * @param margin The margin used to define the edge detection threshold. Defaults to 0.
 * @return [EdgeState] indicating whether the grid item is touching the left edge, right edge, or neither.
 */
fun getGridItemEdgeState(
    x: Int, boundingBoxWidth: Int, screenWidth: Int, margin: Int = 0
): EdgeState {
    val touchesLeft = x <= margin
    val touchesRight = (x + boundingBoxWidth) >= (screenWidth - margin)

    return when {
        touchesLeft -> EdgeState.Left
        touchesRight -> EdgeState.Right
        else -> EdgeState.None
    }
}
