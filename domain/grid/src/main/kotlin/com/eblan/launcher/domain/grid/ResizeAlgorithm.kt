package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

suspend fun resolveConflictsWhenResizing(
    gridItems: MutableList<GridItem>,
    resolveDirection: ResolveDirection,
    resizingGridItem: GridItem,
    rows: Int,
    columns: Int,
): List<GridItem>? {
    return if (resolveConflicts(
            gridItems = gridItems,
            resolveDirection = resolveDirection,
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

