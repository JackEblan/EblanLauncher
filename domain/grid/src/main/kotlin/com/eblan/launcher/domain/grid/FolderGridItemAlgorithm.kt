package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

fun resolveConflictsWhenFolding(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
): List<GridItem>? {
    return if (moveGridItemIntoFolder(
            gridItems = gridItems,
            moving = moving,
        )
    ) {
        gridItems
    } else {
        null
    }
}

private fun moveGridItemIntoFolder(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
): Boolean {
    for (gridItem in gridItems) {
        val isOverlapping = gridItem.id != moving.id && rectanglesOverlap(
            moving = moving,
            other = gridItem,
        )

        if (!isOverlapping) {
            continue
        }

        val data = gridItem.data

        if (data is GridItemData.Folder) {
            val zIndex = data.gridItems.maxOf { it.zIndex }

            val movingIndex = gridItems.indexOfFirst { it.id == moving.id }

            gridItems[movingIndex] = moving.copy(zIndex = zIndex + 1)
        } else {
            val firstIndex = gridItems.indexOfFirst { it.id == gridItem.id }

            val secondIndex = gridItems.indexOfFirst { it.id == moving.id }

            gridItems[firstIndex] = gridItem.copy(zIndex = 0)

            gridItems[secondIndex] = moving.copy(zIndex = 1)
        }

        return true
    }

    return false
}