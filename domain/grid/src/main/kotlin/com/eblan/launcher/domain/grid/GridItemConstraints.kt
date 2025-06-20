package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection

fun isGridItemSpanWithinBounds(gridItem: GridItem, rows: Int, columns: Int): Boolean {
    return gridItem.startRow in 0 until rows && gridItem.startColumn in 0 until columns && gridItem.startRow + gridItem.rowSpan <= rows && gridItem.startColumn + gridItem.columnSpan <= columns
}

fun rectanglesOverlap(moving: GridItem, other: GridItem): Boolean {
    val movingTop = moving.startRow
    val movingBottom = moving.startRow + moving.rowSpan
    val movingLeft = moving.startColumn
    val movingRight = moving.startColumn + moving.columnSpan

    val otherTop = other.startRow
    val otherBottom = other.startRow + other.rowSpan
    val otherLeft = other.startColumn
    val otherRight = other.startColumn + other.columnSpan

    return movingRight > otherLeft && movingLeft < otherRight && movingBottom > otherTop && movingTop < otherBottom
}

fun getResolveDirectionByX(
    gridItem: GridItem,
    x: Int,
    columns: Int,
    gridWidth: Int,
): ResolveDirection {
    val cellWidth = gridWidth / columns

    val gridItemX = gridItem.startColumn * cellWidth

    val gridItemWidth = gridItem.columnSpan * cellWidth

    val xInGridItem = x - gridItemX

    return when {
        xInGridItem < gridItemWidth / 3 -> {
            ResolveDirection.End
        }

        xInGridItem < 2 * gridItemWidth / 3 -> {
            ResolveDirection.Center
        }

        else -> {
            ResolveDirection.Start
        }
    }
}

fun getResolveDirectionByDiff(
    old: GridItem,
    new: GridItem,
): ResolveDirection {
    val oldCenterRow = old.startRow + old.rowSpan / 2.0
    val oldCenterColumn = old.startColumn + old.columnSpan / 2.0

    val newCenterRow = new.startRow + new.rowSpan / 2.0
    val newCenterColumn = new.startColumn + new.columnSpan / 2.0

    val rowDiff = newCenterRow - oldCenterRow
    val columnDiff = newCenterColumn - oldCenterColumn

    return when {
        rowDiff < 0 || columnDiff < 0 -> ResolveDirection.Start
        rowDiff > 0 || columnDiff > 0 -> ResolveDirection.End
        else -> ResolveDirection.Center
    }
}

fun getGridItemByCoordinates(
    id: String,
    gridItems: List<GridItem>,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    gridWidth: Int,
    gridHeight: Int,
): GridItem? {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    return gridItems.find { gridItem ->
        val startColumn = x / cellWidth

        val startRow = y / cellHeight

        val rowInSpan = startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

        val columnInSpan =
            startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

        gridItem.id != id && rowInSpan && columnInSpan
    }
}

fun getResolveDirectionBySpan(
    moving: GridItem,
    other: GridItem,
): ResolveDirection {
    val movingLeft = moving.startColumn
    val movingRight = moving.startColumn + moving.columnSpan

    val otherLeft = other.startColumn
    val otherRight = other.startColumn + other.columnSpan

    val touchesLeft = otherLeft in (movingLeft + 1)..<movingRight
    val touchesRight = otherRight in (movingLeft + 1)..<movingRight

    return when {
        touchesLeft -> ResolveDirection.End
        touchesRight -> ResolveDirection.Start
        else -> ResolveDirection.Center
    }
}