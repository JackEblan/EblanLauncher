package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

/**
 * Resizes a [GridItem] based on new pixel dimensions.
 *
 * This function converts the desired new pixel width and height into grid cell dimensions by taking into
 * account the pixel dimensions of a single grid cell ([gridCellPixelWidth] and [gridCellPixelHeight]). The
 * calculated grid cell dimensions are then used to resize the grid item.
 *
 * @param gridItem The grid item to be resized. If `null`, the function returns `null`.
 * @param newPixelWidth The new desired width in pixels for the grid item.
 * @param newPixelHeight The new desired height in pixels for the grid item.
 * @param gridCellPixelWidth The width of a single grid cell in pixels.
 * @param gridCellPixelHeight The height of a single grid cell in pixels.
 * @return A new [GridItem] with resized grid cells if [gridItem] is not `null`; otherwise, `null`.
 */
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

/**
 * Converts pixel dimensions to grid cell dimensions.
 *
 * This function calculates how many grid cells fit into the specified pixel dimensions based on the pixel
 * size of a single grid cell. The division result is coerced to be at least 1 so that the grid item always
 * occupies a minimum of one cell in each direction.
 *
 * @param newPixelWidth The width in pixels to be converted.
 * @param newPixelHeight The height in pixels to be converted.
 * @param gridCellPixelWidth The width of a single grid cell in pixels.
 * @param gridCellPixelHeight The height of a single grid cell in pixels.
 * @return A [Pair] where the first component is the number of grid cells corresponding to the width and the second
 * component is the number corresponding to the height.
 */
private fun pixelsToGridCells(
    newPixelWidth: Int,
    newPixelHeight: Int,
    gridCellPixelWidth: Int,
    gridCellPixelHeight: Int,
): Pair<Int, Int> {
    val newWidth = (newPixelWidth / gridCellPixelWidth).coerceAtLeast(1)
    val newHeight = (newPixelHeight / gridCellPixelHeight).coerceAtLeast(1)
    return Pair(newWidth, newHeight)
}

/**
 * Calculates the new set of grid cells for a grid item when it is resized.
 *
 * The new grid cells are calculated by first identifying the top-left cell from the list of the current cells
 * (using the smallest row and column values). A rectangular block of grid cells with the specified [newWidth]
 * and [newHeight] is then created starting from this top-left cell.
 *
 * @param oldCells The original list of [GridCell] objects representing the current position of the grid item.
 * @param newWidth The desired new width of the grid item in terms of grid cells.
 * @param newHeight The desired new height of the grid item in terms of grid cells.
 * @return A list of [GridCell] objects representing the new layout of the grid item after resizing.
 */
private fun calculateResizedCells(
    oldCells: List<GridCell>, newWidth: Int, newHeight: Int
): List<GridCell> {
    // Find the top-left cell based on the smallest row and column values.
    val topLeftCell = oldCells.minWith(compareBy({ it.row }, { it.column }))

    val newCells = mutableListOf<GridCell>()
    for (row in topLeftCell.row until topLeftCell.row + newHeight) {
        for (col in topLeftCell.column until topLeftCell.column + newWidth) {
            newCells.add(GridCell(row, col))
        }
    }
    return newCells
}

/**
 * Resizes a [GridItem] to a new size specified in grid cells.
 *
 * This function uses [calculateResizedCells] to compute the new grid cell positions for the grid item based on
 * the desired new width ([newWidth]) and new height ([newHeight]) in grid cells. The original grid item is resized
 * such that its top-left cell remains anchored while the overall dimensions change.
 *
 * @param gridItem The grid item to be resized. If `null`, the function returns `null`.
 * @param newWidth The new desired width of the grid item in terms of grid cells.
 * @param newHeight The new desired height of the grid item in terms of grid cells.
 * @return A new [GridItem] with updated grid cells if [gridItem] is not `null`; otherwise, `null`.
 */
private fun resizeGridItem(
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