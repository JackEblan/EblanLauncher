package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

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

suspend fun findAvailableRegionByPage(
    gridItems: List<GridItem>,
    gridItem: GridItem,
    pageCount: Int,
    rows: Int,
    columns: Int,
): GridItem? {
    for (page in 0..pageCount) {
        for (row in 0..(rows - gridItem.rowSpan)) {
            for (column in 0..(columns - gridItem.columnSpan)) {
                if (!coroutineContext.isActive) return null

                val candidateGridItem = gridItem.copy(
                    page = page,
                    startRow = row,
                    startColumn = column,
                )

                val overlaps = gridItems.any { otherGridItem ->
                    otherGridItem.page == page && rectanglesOverlap(
                        moving = candidateGridItem,
                        other = otherGridItem,
                    )
                }

                if (!overlaps) {
                    return candidateGridItem
                }
            }
        }
    }

    return null
}

fun getWidgetGridItemSpan(
    cellWidth: Int,
    cellHeight: Int,
    minWidth: Int,
    minHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
): Pair<Int, Int> {
    val rowSpan = if (targetCellHeight == 0) {
        (minHeight + cellHeight - 1) / cellHeight
    } else {
        targetCellHeight
    }

    val columnSpan = if (targetCellWidth == 0) {
        (minWidth + cellWidth - 1) / cellWidth
    } else {
        targetCellWidth
    }

    return rowSpan to columnSpan
}

fun getWidgetGridItemSize(
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
    minWidth: Int,
    minHeight: Int,
): Pair<Int, Int> {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val width = if (targetCellWidth > 0) {
        targetCellWidth * cellWidth
    } else {
        minWidth
    }

    val height = if (targetCellHeight > 0) {
        targetCellHeight * cellHeight
    } else {
        minHeight
    }

    return width to height
}

fun groupOverlappingGridItems(gridItems: List<GridItem>): List<List<GridItem>> {
    val visited = mutableSetOf<GridItem>()
    val groups = mutableListOf<List<GridItem>>()

    for (item in gridItems) {
        if (item in visited) continue

        val group = mutableListOf<GridItem>()
        val queue = ArrayDeque<GridItem>()
        queue += item

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue

            visited += current
            group += current

            val neighbors = gridItems.filter { it !in visited && rectanglesOverlap(current, it) }
            queue += neighbors
        }

        if (group.size > 1) {
            groups += group
        }
    }

    return groups
}
