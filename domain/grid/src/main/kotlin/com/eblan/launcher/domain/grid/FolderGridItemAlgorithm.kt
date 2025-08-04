package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

suspend fun resolveConflictsWhenGrouping(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
    folderRows: Int,
    folderColumns: Int,
): List<GridItem>? {
    return if (moveGridItemIntoFolder(
            gridItems = gridItems,
            moving = moving,
            folderRows = folderRows,
            folderColumns = folderColumns,
        )
    ) {
        gridItems
    } else {
        null
    }
}

@OptIn(ExperimentalUuidApi::class)
private suspend fun moveGridItemIntoFolder(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
    folderRows: Int,
    folderColumns: Int,
): Boolean {
    for (gridItem in gridItems) {
        val isOverlapping = gridItem.id != moving.id && rectanglesOverlap(
            moving = moving,
            other = gridItem,
        )

        if (isOverlapping) {
            val data = gridItem.data

            // Check conflicting grid item type
            if (data is GridItemData.Folder) {
                // Find an empty spot inside folder for the moving grid item
                val newGridItem = findAvailableRegion(
                    gridItems = data.gridItems,
                    page = moving.page,
                    gridItem = moving,
                    rows = folderRows,
                    columns = folderColumns,
                )?.copy(folderId = gridItem.folderId) ?: return false

                // Update folderId of the moving
                val movingIndex = gridItems.indexOfFirst { it.id == newGridItem.id }
                gridItems[movingIndex] = newGridItem.copy(folderId = gridItem.folderId)

                // Add the moving to the folder
                val newData = data.copy(gridItems = data.gridItems + newGridItem)

                val conflictingIndex = gridItems.indexOfFirst { it.id == gridItem.id }

                gridItems[conflictingIndex] = gridItem.copy(data = newData)

                return true
            } else {
                val folderId = Uuid.random().toHexString()
                val firstGridItem = gridItem.copy(folderId = folderId)
                val secondGridItem = moveGridItemToRight(
                    moving = moving,
                    conflicting = firstGridItem,
                    rows = folderRows,
                    columns = folderColumns,
                ) ?: return false

                // Update folderId of the moving and conflicting to use same folderId
                val firstIndex = gridItems.indexOfFirst { it.id == gridItem.id }
                val secondIndex = gridItems.indexOfFirst { it.id == moving.id }
                gridItems[firstIndex] = gridItem.copy(folderId = folderId)
                gridItems[secondIndex] = moving.copy(folderId = folderId)

                // Use conflicting grid item as source
                val newGridItem = gridItem.copy(
                    data = GridItemData.Folder(
                        label = "Unknown",
                        gridItems = listOf(firstGridItem, secondGridItem),
                    ),
                )

                gridItems.add(newGridItem)

                return true
            }
        }
    }

    return false
}