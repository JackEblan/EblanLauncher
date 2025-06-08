package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.SideAnchor

fun resizeGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    anchor: Anchor,
): GridItem {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (newWidth, newHeight) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = cellWidth,
        gridCellHeight = cellHeight,
    )

    return resizeGridItem(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
        anchor = anchor,
    )
}

fun resizeWidgetGridItemWithPixels(
    gridItem: GridItem,
    width: Int,
    height: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    anchor: SideAnchor,
): GridItem {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (newWidth, newHeight) = pixelDimensionsToGridSpan(
        width = width,
        height = height,
        gridCellWidth = cellWidth,
        gridCellHeight = cellHeight,
    )

    return resizeGridItemWithSideAnchor(
        gridItem = gridItem,
        newWidth = newWidth,
        newHeight = newHeight,
        anchor = anchor,
    )
}

private fun pixelDimensionsToGridSpan(
    width: Int,
    height: Int,
    gridCellWidth: Int,
    gridCellHeight: Int,
): Pair<Int, Int> {
    val spanWidth = ((width + gridCellWidth - 1) / gridCellWidth).coerceAtLeast(1)

    val spanHeight = ((height + gridCellHeight - 1) / gridCellHeight).coerceAtLeast(1)

    return spanWidth to spanHeight
}

private fun resizeGridItem(
    gridItem: GridItem,
    newWidth: Int,
    newHeight: Int,
    anchor: Anchor,
): GridItem {
    val newStartRow: Int

    val newStartColumn: Int

    when (anchor) {
        Anchor.TopStart -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        Anchor.TopEnd -> {
            // Preserve the top-right corner.
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn + gridItem.columnSpan - newWidth
        }

        Anchor.BottomStart -> {
            // Preserve the bottom-left corner.
            newStartRow = gridItem.startRow + gridItem.rowSpan - newHeight
            newStartColumn = gridItem.startColumn
        }

        Anchor.BottomEnd -> {
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

private fun resizeGridItemWithSideAnchor(
    gridItem: GridItem,
    newWidth: Int,
    newHeight: Int,
    anchor: SideAnchor,
): GridItem {
    val newStartRow: Int

    val newStartColumn: Int

    when (anchor) {
        SideAnchor.Top -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.Bottom -> {
            newStartRow = gridItem.startRow + gridItem.rowSpan - newHeight
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.Left -> {
            newStartRow = gridItem.startRow
            newStartColumn = gridItem.startColumn
        }

        SideAnchor.Right -> {
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