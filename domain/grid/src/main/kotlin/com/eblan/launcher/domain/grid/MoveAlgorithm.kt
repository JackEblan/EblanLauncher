package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenMoving(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    movingGridItem: GridItem,
    x: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
): List<GridItem>? {
    return if (resolveConflicts(
            gridItems = gridItems,
            resolveDirection = resolveDirection,
            movingGridItem = movingGridItem,
            x = x,
            rows = rows,
            columns = columns,
            gridWidth = gridWidth,
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
    movingGridItem: GridItem,
    x: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
): Boolean {
    for (gridItem in gridItems) {
        if (!coroutineContext.isActive) return false

        // Skip the moving grid item itself.
        if (gridItem.id == movingGridItem.id) continue

        if (rectanglesOverlap(movingGridItem = movingGridItem, gridItem = gridItem)) {
            val movedGridItem = moveGridItem(
                resolveDirection = resolveDirection,
                movingGridItem = movingGridItem,
                conflictingGridItem = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            // Update the grid with the shifted item.
            val index = gridItems.indexOfFirst { it.id == gridItem.id }

            gridItems[index] = movedGridItem

            // Recursively resolve further conflicts from the shifted item.
            if (!resolveConflicts(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movedGridItem,
                    x = x,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                )
            ) {
                return false
            }
        }
    }

    return true
}

fun moveGridItem(
    resolveDirection: ResolveDirection?,
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    return when (resolveDirection) {
        ResolveDirection.Start -> {
            moveGridItemToLeft(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.End -> {
            moveGridItemToRight(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
                rows = rows,
                columns = columns,
            )
        }

        ResolveDirection.Center, null -> null
    }
}

private fun moveGridItemToRight(
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn + movingGridItem.columnSpan
    var newRow = conflictingGridItem.startRow

    // Wrap horizontally if necessary.
    if (newColumn + conflictingGridItem.columnSpan > columns) {
        newColumn = 0
        newRow = movingGridItem.startRow + movingGridItem.rowSpan
    }

    // Check vertical bounds.
    if (newRow + conflictingGridItem.rowSpan > rows) {
        return null // No space left.
    }

    return conflictingGridItem.copy(startRow = newRow, startColumn = newColumn)
}

private fun moveGridItemToLeft(
    movingGridItem: GridItem,
    conflictingGridItem: GridItem,
    rows: Int,
    columns: Int,
): GridItem? {
    var newColumn = movingGridItem.startColumn - conflictingGridItem.columnSpan
    var newRow = conflictingGridItem.startRow

    // Wrap horizontally if necessary
    if (newColumn < 0) {
        newColumn = columns - conflictingGridItem.columnSpan
        newRow = conflictingGridItem.startRow - 1
    }

    // Check vertical bounds
    if (newRow < 0) {
        return null // No space left
    }

    if (newRow + conflictingGridItem.rowSpan > rows) {
        return null
    }

    return conflictingGridItem.copy(startRow = newRow, startColumn = newColumn)
}

