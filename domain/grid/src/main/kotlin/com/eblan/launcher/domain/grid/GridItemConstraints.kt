package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemMovement

/**
 * Validates that grid item span within the acceptable grid bounds.
 *
 * For each grid item, it checks that:
 * - The starting row and column are within bounds.
 * - The entire region (startRow + rowSpan, startColumn + colSpan) fits within the grid.
 *
 * @param gridItem to validate.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @return `true` if all grid items are within bounds; `false` if any item exceeds the grid limits.
 */
fun isGridItemSpanWithinBounds(gridItem: GridItem, rows: Int, columns: Int): Boolean {
    return gridItem.startRow in 0 until rows && gridItem.startColumn in 0 until columns && gridItem.startRow + gridItem.rowSpan <= rows && gridItem.startColumn + gridItem.columnSpan <= columns
}

/**
 * Converts pixel coordinates (x, y) into the corresponding grid cell indices (startRow, startColumn).
 *
 * The grid is organized as a matrix with a fixed number of rows and columns.
 * The size of a single grid cell is determined by the overall screen dimensions:
 *   - cellWidth = screenWidth / columns
 *   - cellHeight = screenHeight / rows
 *
 * The function then computes:
 *   - startColumn = x / cellWidth  (the horizontal index)
 *   - startRow = y / cellHeight    (the vertical index)
 *
 * These indices represent the grid cell in which the given pixel coordinates fall.
 *
 * @param x The x-coordinate in pixels.
 * @param y The y-coordinate in pixels.
 * @param rows The total number of rows in the grid.
 * @param columns The total number of columns in the grid.
 * @param screenWidth The total width of the screen (or container) in pixels.
 * @param screenHeight The total height of the screen (or container) in pixels.
 * @return A [Pair] where the first element is the start row and the second element is the start column.
 */
fun coordinatesToStartPosition(
    x: Int,
    y: Int,
    rows: Int,
    columns: Int,
    screenWidth: Int,
    screenHeight: Int,
): Pair<Int, Int> {
    val cellWidth = screenWidth / columns
    val cellHeight = screenHeight / rows

    val startColumn = x / cellWidth
    val startRow = y / cellHeight

    return startRow to startColumn
}

/**
 * Checks if two grid items overlap.
 *
 * @param movingGridItem The first grid item.
 * @param gridItem The second grid item.
 * @return True if the rectangular regions defined by the items overlap.
 */
fun rectanglesOverlap(movingGridItem: GridItem, gridItem: GridItem): Boolean {
    if (movingGridItem.startRow + movingGridItem.rowSpan <= gridItem.startRow || gridItem.startRow + gridItem.rowSpan <= movingGridItem.startRow) {
        return false
    }
    if (movingGridItem.startColumn + movingGridItem.columnSpan <= gridItem.startColumn || gridItem.startColumn + gridItem.columnSpan <= movingGridItem.startColumn) {
        return false
    }
    return true
}
