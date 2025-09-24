/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection

fun isGridItemSpanWithinBounds(
    gridItem: GridItem,
    columns: Int,
    rows: Int
): Boolean {
    return gridItem.startColumn in 0 until columns &&
            gridItem.startRow in 0 until rows &&
            gridItem.startColumn + gridItem.columnSpan <= columns &&
            gridItem.startRow + gridItem.rowSpan <= rows
}

fun rectanglesOverlap(moving: GridItem, other: GridItem): Boolean {
    val movingLeft = moving.startColumn
    val movingRight = moving.startColumn + moving.columnSpan
    val movingTop = moving.startRow
    val movingBottom = moving.startRow + moving.rowSpan

    val otherLeft = other.startColumn
    val otherRight = other.startColumn + other.columnSpan
    val otherTop = other.startRow
    val otherBottom = other.startRow + other.rowSpan

    return movingRight > otherLeft &&
            movingLeft < otherRight &&
            movingBottom > otherTop &&
            movingTop < otherBottom
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
            ResolveDirection.Right
        }

        xInGridItem < 2 * gridItemWidth / 3 -> {
            ResolveDirection.Center
        }

        else -> {
            ResolveDirection.Left
        }
    }
}

fun getGridItemByCoordinates(
    id: String,
    gridItems: List<GridItem>,
    columns: Int,
    rows: Int,
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

        val columnInSpan =
            startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

        val rowInSpan = startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

        gridItem.id != id && rowInSpan && columnInSpan
    }
}

fun getRelativeResolveDirection(
    moving: GridItem,
    other: GridItem,
): ResolveDirection {
    return when {
        moving.startColumn < other.startColumn -> ResolveDirection.Right

        moving.startColumn > other.startColumn -> ResolveDirection.Left

        moving.startRow > other.startRow -> ResolveDirection.Left

        moving.startRow < other.startRow -> ResolveDirection.Right

        else -> ResolveDirection.Center
    }
}

fun findAvailableRegionByPage(
    gridItems: List<GridItem>,
    gridItem: GridItem,
    pageCount: Int,
    columns: Int,
    rows: Int,
): GridItem? {
    for (page in 0 until pageCount) {
        for (row in 0..(rows - gridItem.rowSpan)) {
            for (column in 0..(columns - gridItem.columnSpan)) {
                val candidateGridItem = gridItem.copy(
                    page = page,
                    startColumn = column,
                    startRow = row,
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

    return columnSpan to rowSpan
}

fun getWidgetGridItemSize(
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    minWidth: Int,
    minHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
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
