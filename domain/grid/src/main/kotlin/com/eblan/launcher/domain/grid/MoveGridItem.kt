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

fun moveGridItem(
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

        ResolveDirection.Center -> throw IllegalStateException()
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

    return conflicting.copy(startRow = newStartRow, startColumn = newStartColumn)
}

