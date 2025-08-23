package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenMoving(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    moving: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (
        resolveConflictsIterative(
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

private suspend fun resolveConflictsIterative(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    moving: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    val queue = ArrayDeque<GridItem>()
    queue.add(moving)

    while (queue.isNotEmpty() && coroutineContext.isActive) {
        val current = queue.removeFirst()

        for (gridItem in gridItems) {
            val isOverlapping = gridItem.id != current.id &&
                    rectanglesOverlap(current, gridItem)

            if (isOverlapping) {
                val movedGridItem = moveGridItem(
                    resolveDirection = resolveDirection,
                    moving = current,
                    conflicting = gridItem,
                    rows = rows,
                    columns = columns,
                ) ?: return false

                val index = gridItems.indexOfFirst { it.id == gridItem.id }

                gridItems[index] = movedGridItem

                queue.add(movedGridItem)
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
        ResolveDirection.EndToStart -> {
            moveGridItemFromEndToStart(
                moving = moving,
                conflicting = conflicting,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.StartToEnd -> {
            moveGridItemFromStartToEnd(
                moving = moving,
                conflicting = conflicting,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.Center -> throw IllegalStateException()
    }
}

fun moveGridItemFromStartToEnd(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = moving.startColumn + moving.columnSpan
    var newRow = conflicting.startRow

    if (newColumn + conflicting.columnSpan > columns) {
        newColumn = 0
        newRow = moving.startRow + moving.rowSpan
    }

    if (newRow + conflicting.rowSpan > rows) {
        return null
    }

    return conflicting.copy(startRow = newRow, startColumn = newColumn)
}

private fun moveGridItemFromEndToStart(
    moving: GridItem,
    conflicting: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = moving.startColumn - conflicting.columnSpan
    var newRow = conflicting.startRow

    if (newColumn < 0) {
        newColumn = columns - conflicting.columnSpan
        newRow = conflicting.startRow - 1
    }

    if (newRow < 0) {
        return null
    }

    if (newRow + conflicting.rowSpan > rows) {
        return null
    }

    return conflicting.copy(startRow = newRow, startColumn = newColumn)
}

