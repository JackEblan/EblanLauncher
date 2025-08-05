package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem

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