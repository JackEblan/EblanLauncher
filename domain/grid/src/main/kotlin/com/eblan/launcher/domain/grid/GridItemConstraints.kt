package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

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
 * Checks if two grid items overlap.
 *
 * @param movingGridItem The first grid item.
 * @param gridItem The second grid item.
 * @return True if the rectangular regions defined by the items overlap.
 */
fun rectanglesOverlap(movingGridItem: GridItem, gridItem: GridItem): Boolean {
    val noOverlap = movingGridItem.startRow + movingGridItem.rowSpan <= gridItem.startRow ||
            gridItem.startRow + gridItem.rowSpan <= movingGridItem.startRow ||
            movingGridItem.startColumn + movingGridItem.columnSpan <= gridItem.startColumn ||
            gridItem.startColumn + gridItem.columnSpan <= movingGridItem.startColumn

    return !noOverlap
}