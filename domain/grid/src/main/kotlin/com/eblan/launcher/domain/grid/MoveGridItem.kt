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
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenMoving(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    moving: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (
        resolveConflicts(
            gridItems = gridItems,
            resolveDirection = resolveDirection,
            moving = moving,
            rows = rows,
            columns = columns,
        )
    ) {
        gridItems
    } else {
        null
    }
}

private suspend fun resolveConflicts(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    moving: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for ((index, gridItem) in gridItems.withIndex()) {
        coroutineContext.ensureActive()

        val isOverlapping = gridItem.id != moving.id &&
            rectanglesOverlap(moving, gridItem)

        if (isOverlapping) {
            val movedGridItem = moveGridItem(
                resolveDirection = resolveDirection,
                moving = moving,
                conflicting = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            gridItems[index] = movedGridItem

            if (!resolveConflicts(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    moving = movedGridItem,
                    rows = rows,
                    columns = columns,
                )
            ) {
                return false
            }
        }
    }

    return true
}

private fun moveGridItem(
    resolveDirection: ResolveDirection,
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    return when (resolveDirection) {
        ResolveDirection.Left -> {
            moveGridItemToLeft(
                moving = moving,
                conflicting = conflicting,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.Right -> {
            moveGridItemToRight(
                moving = moving,
                conflicting = conflicting,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.Center -> null
    }
}

private fun moveGridItemToRight(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newStartColumn = moving.startColumn + moving.columnSpan
    var newStartRow = conflicting.startRow

    if (newStartColumn + conflicting.columnSpan > columns) {
        newStartColumn = 0
        newStartRow = moving.startRow + moving.rowSpan
    }

    if (newStartRow + conflicting.rowSpan > rows) {
        return null
    }

    return conflicting.copy(
        startRow = newStartRow,
        startColumn = newStartColumn,
    )
}

private fun moveGridItemToLeft(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newStartColumn = moving.startColumn - conflicting.columnSpan
    var newStartRow = conflicting.startRow

    if (newStartColumn < 0) {
        newStartColumn = columns - conflicting.columnSpan
        newStartRow = moving.startRow - 1
    }

    if (newStartRow < 0) {
        return null
    }

    if (newStartRow + conflicting.rowSpan > rows) {
        return null
    }

    return conflicting.copy(
        startRow = newStartRow,
        startColumn = newStartColumn,
    )
}
