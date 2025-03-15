package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.SideAnchor

/**
 * Resizes a [GridItem] based on new pixel dimensions.
 *
 * Converts the desired new pixel width and height into grid cell dimensions by taking into account
 * the size of a single grid cell ([gridCellWidth] and [gridCellHeight]). The calculated grid cell dimensions
 * are then used to resize the grid item by updating its spans and (depending on [anchor]) its starting position.
 *
 * @param gridItem The grid item to be resized.
 * @param width The new desired width in pixels for the grid item.
 * @param height The new desired height in pixels for the grid item.
 * @param gridCellWidth The width of a single grid cell in pixels.
 * @param gridCellHeight The height of a single grid cell in pixels.
 * @param anchor The corner that remains fixed during resizing.
 * @return A new [GridItem] with updated starting position and spans.
 */
fun resizeGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
    anchor: Anchor,
): GridItem {
    val (newWidth, newHeight) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = gridCellWidth,
        gridCellHeight = gridCellHeight,
    )

    return resizeGridItem(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
        anchor = anchor,
    )
}

/**
 * Resizes a [GridItem] based on new pixel dimensions.
 *
 * Converts the desired new pixel width and height into grid cell dimensions by taking into account
 * the size of a single grid cell ([gridCellWidth] and [gridCellHeight]). The calculated grid cell dimensions
 * are then used to resize the grid item by updating its spans and (depending on [anchor]) its starting position.
 *
 * @param gridItem The grid item to be resized.
 * @param width The new desired width in pixels for the grid item.
 * @param height The new desired height in pixels for the grid item.
 * @param gridCellWidth The width of a single grid cell in pixels.
 * @param gridCellHeight The height of a single grid cell in pixels.
 * @param anchor The side that remains fixed during resizing.
 * @return A new [GridItem] with updated starting position and spans.
 */
fun resizeWidgetGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
    anchor: SideAnchor,
): GridItem {
    val (newWidth, newHeight) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = gridCellWidth,
        gridCellHeight = gridCellHeight,
    )

    return resizeGridItemWithSideAnchor(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
        anchor = anchor,
    )
}

/**
 * Converts pixel dimensions into grid span counts.
 *
 * Given a width and height in pixels, this function calculates how many grid cells
 * (horizontally and vertically) can be covered based on the dimensions of a single grid cell.
 * The results are coerced to be at least 1, ensuring that even very small pixel dimensions count as one cell.
 *
 * @param width The width in pixels to be converted.
 * @param height The height in pixels to be converted.
 * @param gridCellWidth The width of a single grid cell in pixels.
 * @param gridCellHeight The height of a single grid cell in pixels.
 * @return A [Pair] where the first component is the number of cells that fit horizontally, and the second is the number of cells that fit vertically.
 */
private fun pixelDimensionsToGridSpan(
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
): Pair<Int, Int> {
    // Calculate the number of cells by rounding up (ceiling division).
    val spanWidth = ((width + gridCellWidth - 1) / gridCellWidth).coerceAtLeast(1)
    val spanHeight = ((height + gridCellHeight - 1) / gridCellHeight).coerceAtLeast(1)
    return spanWidth to spanHeight
}

/**
 * Resizes a [GridItem] to a new size specified in grid cells.
 *
 * Using the new width ([newWidth]) and height ([newHeight])—which represent the new column and row spans—
 * this function updates the grid item. Depending on the [anchor] value, the grid item’s starting position
 * is adjusted so that a specific corner remains fixed.
 *
 * For example:
 * - [Anchor.TOP_START]: The top‑left corner remains fixed.
 * - [Anchor.TOP_END]: The top‑right corner remains fixed.
 * - [Anchor.BOTTOM_START]: The bottom‑left corner remains fixed.
 * - [Anchor.BOTTOM_END]: The bottom‑right corner remains fixed.
 *
 * @param gridItem The grid item to be resized.
 * @param newWidth The new desired width in grid cells (new column span).
 * @param newHeight The new desired height in grid cells (new row span).
 * @param anchor The fixed corner during the resize.
 * @return A new [GridItem] with updated starting position and spans.
 */
private fun resizeGridItem(
    gridItem: GridItem,
    newWidth: Int,
    newHeight: Int,
    anchor: Anchor,
): GridItem {
    val newStartRow: Int
    val newStartColumn: Int

    when (anchor) {
        Anchor.TOP_START -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        Anchor.TOP_END -> {
            // Preserve the top-right corner.
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - newWidth
        }

        Anchor.BOTTOM_START -> {
            // Preserve the bottom-left corner.
            newStartRow = gridItem.startRow + gridItem.rowSpan - newHeight
            newStartColumn = gridItem.startColumn
        }

        Anchor.BOTTOM_END -> {
            // Preserve the bottom-right corner.
            newStartRow = gridItem.startRow + gridItem.rowSpan - newHeight
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - newWidth
        }
    }

    return gridItem.copy(
        startRow = newStartRow,
        startColumn = newStartColumn,
        rowSpan = newHeight,
        columnSpan = newWidth,
    )
}

/**
 * Resizes a [GridItem] to a new size specified in grid cells,
 * using a single side anchor to determine the fixed edge.
 *
 * When the anchor is:
 * - TOP: the top edge remains fixed; the bottom edge expands or contracts.
 * - BOTTOM: the bottom edge remains fixed; the top edge moves upward.
 * - LEFT: the left edge remains fixed; the right edge expands or contracts.
 * - RIGHT: the right edge remains fixed; the left edge shifts.
 *
 * @param gridItem The grid item to be resized.
 * @param newWidth The new desired width in grid cells (column span).
 * @param newHeight The new desired height in grid cells (row span).
 * @param anchor The side that remains fixed during resizing.
 * @return A new [GridItem] with updated starting position and spans.
 */
private fun resizeGridItemWithSideAnchor(
    gridItem: GridItem,
    newWidth: Int,
    newHeight: Int,
    anchor: SideAnchor,
): GridItem {
    val newStartRow: Int
    val newStartColumn: Int

    when (anchor) {
        SideAnchor.TOP -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.BOTTOM -> {
            newStartRow = gridItem.startRow + gridItem.rowSpan - newHeight
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.LEFT -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.RIGHT -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - newWidth
        }
    }

    return gridItem.copy(
        startRow = newStartRow,
        startColumn = newStartColumn,
        rowSpan = newHeight,
        columnSpan = newWidth,
    )
}