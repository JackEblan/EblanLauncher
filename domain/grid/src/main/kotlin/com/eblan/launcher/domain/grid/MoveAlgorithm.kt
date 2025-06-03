package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenMoving(
    gridItems: MutableList<GridItem>,
    movingGridItem: GridItem,
    x: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
): List<GridItem>? {
    val resolveDirection = gridItems.find { gridItem ->
        gridItem.id != movingGridItem.id && rectanglesOverlap(
            movingGridItem = movingGridItem,
            gridItem = gridItem,
        )
    }?.let { gridItem ->
        val cellWidth = gridWidth / columns

        val gridItemX = gridItem.startColumn * cellWidth

        val gridItemWidth = gridItem.columnSpan * cellWidth

        val xInGridItem = x - gridItemX

        when {
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
    } ?: ResolveDirection.Center

    return if (resolveConflicts(
            gridItems = gridItems,
            movingGridItem = movingGridItem,
            resolveDirection = resolveDirection,
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
    movingGridItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in gridItems) {
        if (!coroutineContext.isActive) return false

        // Skip the moving grid item itself.
        if (gridItem.id == movingGridItem.id) continue

        if (rectanglesOverlap(movingGridItem = movingGridItem, gridItem = gridItem)) {
            val shiftedItem = moveGridItem(
                resolveDirection = resolveDirection,
                movingGridItem = movingGridItem,
                conflictingGridItem = gridItem,
                rows = rows,
                columns = columns,
            ) ?: return false

            // Update the grid with the shifted item.
            val index = gridItems.indexOfFirst { it.id == gridItem.id }

            gridItems[index] = shiftedItem

            // Recursively resolve further conflicts from the shifted item.
            if (!resolveConflicts(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = shiftedItem,
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