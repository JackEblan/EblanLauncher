package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItemWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData.Folder
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.sqrt

private const val maxColumns = 5

private const val maxRows = 4

internal fun FolderGridItemWrapper.asGridItem(): GridItem {
    val gridItemsByPage = applicationInfoGridItems.getGridItemsByPage()
    val firstPageGridItems = gridItemsByPage[0] ?: emptyList()

    val (columns, rows) = getGridDimension(count = firstPageGridItems.size)

    val data =  Folder(
        id = folderGridItem.id,
        label = folderGridItem.label,
        gridItems = applicationInfoGridItems,
        gridItemsByPage = gridItemsByPage,
        previewGridItemsByPage = gridItemsByPage.values.firstOrNull() ?: emptyList(),
        icon = folderGridItem.icon,
        columns = columns,
        rows = rows,
    )

    return GridItem(
        id = folderGridItem.id,
        page = folderGridItem.page,
        startColumn = folderGridItem.startColumn,
        startRow = folderGridItem.startRow,
        columnSpan = folderGridItem.columnSpan,
        rowSpan = folderGridItem.rowSpan,
        data = data,
        associate = folderGridItem.associate,
        override = folderGridItem.override,
        gridItemSettings = folderGridItem.gridItemSettings,
        doubleTap = folderGridItem.doubleTap,
        swipeUp = folderGridItem.swipeUp,
        swipeDown = folderGridItem.swipeDown
    )
}

internal fun List<ApplicationInfoGridItem>.getGridItemsByPage(): Map<Int, List<ApplicationInfoGridItem>> =
    sortedBy { it.index }
        .chunked(maxColumns * maxRows)
        .mapIndexed { pageIndex, pageItems -> pageIndex to pageItems }
        .toMap()

private fun getGridDimension(count: Int): Pair<Int, Int> {
    if (count <= 0) return 0 to 0

    val columns = min(maxColumns, ceil(sqrt(count.toDouble())).toInt())
    val rows = min(maxRows, ceil(count / columns.toDouble()).toInt())

    return columns to rows
}