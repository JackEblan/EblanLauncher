package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

suspend fun folderGridItem(
    gridItems: MutableList<GridItem>,
    moving: GridItem,
    folderRows: Int,
    folderColumns: Int,
): GridItem? {
    for (gridItem in gridItems) {
        val isOverlapping = gridItem.id != moving.id &&
                rectanglesOverlap(
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
                )

                if (newGridItem != null) {
                    // Add the moving into the folder
                    val newData = data.copy(gridItems + newGridItem)

                    return gridItem.copy(data = newData)
                }
            } else {
                // Add the moving grid item at the end of the conflicting one
                val secondGridItem = moving.copy(

                )

                val newData = GridItemData.Folder(
                    listOf(
                        gridItem,
                        secondGridItem,
                    ),
                )

                return GridItem(
                    id = id,
                    page = page,
                    startRow = startRow,
                    startColumn = startColumn,
                    rowSpan = rowSpan,
                    columnSpan = columnSpan,
                    data = data,
                    associate = associate,
                )
            }
        }
    }

    return null
}