package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

fun moveGridItemIntoFolder(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
    conflicting: GridItem,
): List<GridItem>? {
    if (moving.data is GridItemData.Folder) return null

    val data = conflicting.data

    if (data is GridItemData.Folder) {
        val zIndex = data.gridItems.maxOf { it.zIndex }

        val conflictingIndex = gridItems.indexOfFirst { it.id == conflicting.id }

        gridItems[conflictingIndex] = conflicting.copy(zIndex = zIndex)
    } else {
        val conflictingIndex = gridItems.indexOfFirst { it.id == conflicting.id }

        val movingIndex = gridItems.indexOfFirst { it.id == moving.id }

        gridItems[conflictingIndex] = conflicting.copy(zIndex = 0)

        gridItems[movingIndex] = moving.copy(zIndex = 1)
    }

    return gridItems
}