package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenMoving(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    moving: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (resolveConflicts(
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
    for (gridItem in gridItems) {
        coroutineContext.ensureActive()

        val isOverlapping = gridItem.id != moving.id &&
                rectanglesOverlap(
                    moving = moving,
                    other = gridItem,
                )

        if (isOverlapping) {
            val movedGridItem = moveGridItem(
                resolveDirection = resolveDirection,
                moving = moving,
                conflicting = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            val index = gridItems.indexOfFirst { it.id == gridItem.id }

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
        ResolveDirection.Start -> {
            moveGridItemToLeft(
                moving = moving,
                conflicting = conflicting,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.End -> {
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

fun moveGridItemToRight(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = moving.startColumn + moving.columnSpan
    var newRow = conflicting.startRow

    // Wrap horizontally if necessary.
    if (newColumn + conflicting.columnSpan > columns) {
        newColumn = 0
        newRow = moving.startRow + moving.rowSpan
    }

    // Check vertical bounds.
    if (newRow + conflicting.rowSpan > rows) {
        return null // No space left.
    }

    return conflicting.copy(startRow = newRow, startColumn = newColumn)
}

private fun moveGridItemToLeft(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = moving.startColumn - conflicting.columnSpan
    var newRow = conflicting.startRow

    // Wrap horizontally if necessary
    if (newColumn < 0) {
        newColumn = columns - conflicting.columnSpan
        newRow = conflicting.startRow - 1
    }

    // Check vertical bounds
    if (newRow < 0) {
        return null // No space left
    }

    if (newRow + conflicting.rowSpan > rows) {
        return null
    }

    return conflicting.copy(startRow = newRow, startColumn = newColumn)
}

