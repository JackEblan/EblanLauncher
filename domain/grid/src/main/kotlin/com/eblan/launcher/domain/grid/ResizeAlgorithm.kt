package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenResizing(
    gridItems: MutableList<GridItem>,
    oldGridItem: GridItem,
    resizingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    val gridItemShift = getGridItemShift(
        oldGridItem = oldGridItem,
        resizingGridItem = resizingGridItem,
    )

    return if (resolveConflicts(
            gridItems = gridItems,
            resolveDirection = gridItemShift,
            resizingGridItem = resizingGridItem,
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
    resizingGridItem: GridItem,
    rows: Int,
    columns: Int,
): Boolean {
    for (gridItem in gridItems) {
        if (!coroutineContext.isActive) return false

        // Skip the moving grid item itself.
        if (gridItem.id == resizingGridItem.id) continue

        if (rectanglesOverlap(movingGridItem = resizingGridItem, gridItem = gridItem)) {
            val shiftedItem = moveGridItem(
                resolveDirection = resolveDirection,
                movingGridItem = resizingGridItem,
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
                    resizingGridItem = shiftedItem,
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

private fun getGridItemShift(
    oldGridItem: GridItem,
    resizingGridItem: GridItem,
): ResolveDirection {
    val oldCenterRow = oldGridItem.startRow + oldGridItem.rowSpan / 2.0
    val oldCenterColumn = oldGridItem.startColumn + oldGridItem.columnSpan / 2.0

    val newCenterRow = resizingGridItem.startRow + resizingGridItem.rowSpan / 2.0
    val newCenterColumn = resizingGridItem.startColumn + resizingGridItem.columnSpan / 2.0

    val rowDiff = newCenterRow - oldCenterRow
    val columnDiff = newCenterColumn - oldCenterColumn

    return when {
        rowDiff < 0 || columnDiff < 0 -> ResolveDirection.Start
        rowDiff > 0 || columnDiff > 0 -> ResolveDirection.End
        else -> ResolveDirection.Center
    }
}