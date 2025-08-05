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

fun groupOverlappingGridItems(gridItems: List<GridItem>): List<List<GridItem>> {
    val visited = mutableSetOf<GridItem>()
    val groups = mutableListOf<List<GridItem>>()

    for (item in gridItems) {
        if (item in visited) continue

        val group = mutableListOf<GridItem>()
        val queue = ArrayDeque<GridItem>()
        queue += item

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current in visited) continue

            visited += current
            group += current

            val neighbors = gridItems.filter { it !in visited && rectanglesOverlap(current, it) }
            queue += neighbors
        }

        if (group.size > 1) {
            groups += group
        }
    }

    return groups
}