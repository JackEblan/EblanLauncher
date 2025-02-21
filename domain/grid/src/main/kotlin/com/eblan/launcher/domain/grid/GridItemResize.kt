package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridCell
import com.eblan.launcher.domain.model.GridItem

/**
 * Resizes a [GridItem] based on new pixel dimensions.
 *
 * Converts the desired new pixel width and height into grid cell dimensions by taking into
 * account the pixel dimensions of a single grid cell ([gridCellWidth] and [gridCellHeight]). The
 * calculated grid cell dimensions are then used to resize the grid item.
 *
 * @param gridItem The grid item to be resized. If `null`, the function returns `null`.
 * @param width The new desired width in pixels for the grid item.
 * @param height The new desired height in pixels for the grid item.
 * @param gridCellWidth The width of a single grid cell in pixels.
 * @param gridCellHeight The height of a single grid cell in pixels.
 * @param anchor The starting point of cells expansion.
 * @return A new [GridItem] with resized grid cells if [gridItem] is not `null`; otherwise, `null`.
 */
fun resizeGridItemWithPixels(
    gridItem: GridItem?,
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
    anchor: Anchor,
): GridItem? {
    val (newWidth, newHeight) = pixelsToGridCells(
        width = width,
        height = height,
        gridCellWidth = gridCellWidth,
        gridCellHeight = gridCellHeight
    )

    return resizeGridItem(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
        anchor = anchor,
    )
}

/**
 * Converts pixel dimensions to grid cell dimensions.
 *
 * Calculates how many grid cells fit into the specified pixel dimensions based on the pixel
 * size of a single grid cell. The division result is coerced to be at least 1 so that the grid item always
 * occupies a minimum of one cell in each direction.
 *
 * @param width The width in pixels to be converted.
 * @param height The height in pixels to be converted.
 * @param gridCellWidth The width of a single grid cell in pixels.
 * @param gridCellHeight The height of a single grid cell in pixels.
 * @return A [Pair] where the first component is the number of grid cells corresponding to the width and the second
 * component is the number corresponding to the height.
 */
private fun pixelsToGridCells(
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
): Pair<Int, Int> {
    val newWidth = (width / gridCellWidth).coerceAtLeast(1)
    val newHeight = (height / gridCellHeight).coerceAtLeast(1)
    return Pair(newWidth, newHeight)
}

/**
 * Calculates the new set of grid cells for a grid item when it is resized.
 *
 * The new grid cells are calculated by first identifying the top-left cell from the list of the current cells
 * (using the smallest row and column values). A rectangular block of grid cells with the specified [newWidth]
 * and [newHeight] is then created starting from [anchor].
 *
 * @param oldCells The original list of [GridCell] objects representing the current position of the grid item.
 * @param newWidth The desired new width of the grid item in terms of grid cells.
 * @param newHeight The desired new height of the grid item in terms of grid cells.
 * @param anchor The starting point of cells expansion.
 * @return A list of [GridCell] objects representing the new layout of the grid item after resizing.
 */
private fun calculateResizedCells(
    oldCells: List<GridCell>,
    newWidth: Int,
    newHeight: Int,
    anchor: Anchor,
): List<GridCell> {
    if (oldCells.isEmpty()) return emptyList()

    val startCell = when (anchor) {
        Anchor.TOP_START -> oldCells.minWith(compareBy({ it.row }, { it.column }))

        Anchor.TOP_END -> {
            val topRight = oldCells.minWith(compareBy { it.row })
            val maxCol = oldCells.maxOf { it.column }
            GridCell(topRight.row, maxCol - (newWidth - 1))
        }

        Anchor.BOTTOM_START -> {
            val bottomLeft = oldCells.minWith(compareBy { it.column })
            val maxRow = oldCells.maxOf { it.row }
            GridCell(maxRow - (newHeight - 1), bottomLeft.column)
        }

        Anchor.BOTTOM_END -> {
            val bottomRight = oldCells.maxWith(compareBy({ it.row }, { it.column }))
            GridCell(bottomRight.row - (newHeight - 1), bottomRight.column - (newWidth - 1))
        }
    }

    return List(newHeight) { rowOffset ->
        List(newWidth) { colOffset ->
            GridCell(startCell.row + rowOffset, startCell.column + colOffset)
        }
    }.flatten()
}

/**
 * Resizes a [GridItem] to a new size specified in grid cells.
 *
 * Uses [calculateResizedCells] to compute the new grid cell positions for the grid item based on
 * the desired new width ([newWidth]) and new height ([newHeight]) in grid cells. The original grid item is resized
 * such that [anchor] remains anchored while the overall dimensions change.
 *
 * @param gridItem The grid item to be resized. If `null`, the function returns `null`.
 * @param newWidth The new desired width of the grid item in terms of grid cells.
 * @param newHeight The new desired height of the grid item in terms of grid cells.
 * @param anchor The starting point of cells expansion.
 * @return A new [GridItem] with updated grid cells if [gridItem] is not `null`; otherwise, `null`.
 */
private fun resizeGridItem(
    gridItem: GridItem?,
    newWidth: Int,
    newHeight: Int,
    anchor: Anchor,
): GridItem? {
    return if (gridItem != null) {
        val newCells = calculateResizedCells(
            oldCells = gridItem.cells,
            newWidth = newWidth,
            newHeight = newHeight,
            anchor = anchor,
        )
        gridItem.copy(cells = newCells)
    } else {
        null
    }
}