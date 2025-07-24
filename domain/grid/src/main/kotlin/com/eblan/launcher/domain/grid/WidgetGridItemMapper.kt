package com.eblan.launcher.domain.grid

import android.appwidget.AppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

fun getWidgetGridItem(
    page: Int,
    rows: Int,
    columns: Int,
    componentName: String,
    configure: String?,
    packageName: String,
    targetCellHeight: Int,
    targetCellWidth: Int,
    minWidth: Int,
    minHeight: Int,
    resizeMode: Int,
    minResizeWidth: Int,
    minResizeHeight: Int,
    maxResizeWidth: Int,
    maxResizeHeight: Int,
    preview: String?,
    gridWidth: Int,
    gridHeight: Int,
): GridItem {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val (checkedRowSpan, checkedColumnSpan) = getSpan(
        cellHeight = cellHeight,
        cellWidth = cellWidth,
        minHeight = minHeight,
        minWidth = minWidth,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
    )

    val (checkedMinWidth, checkedMinHeight) = getSize(
        columns = columns,
        gridHeight = gridHeight,
        gridWidth = gridWidth,
        minHeight = minHeight,
        minWidth = minWidth,
        rows = rows,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
    )

    val data = GridItemData.Widget(
        appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID,
        componentName = componentName,
        configure = configure,
        minWidth = checkedMinWidth,
        minHeight = checkedMinHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
    )

    return GridItem(
        id = 0,
        page = page,
        startRow = 0,
        startColumn = 0,
        rowSpan = checkedRowSpan,
        columnSpan = checkedColumnSpan,
        dataId = packageName,
        data = data,
        associate = Associate.Grid,
    )
}

fun getSpan(
    cellWidth: Int,
    cellHeight: Int,
    minWidth: Int,
    minHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
): Pair<Int, Int> {
    val rowSpan = if (targetCellHeight == 0) {
        (minHeight + cellHeight - 1) / cellHeight
    } else {
        targetCellHeight
    }

    val columnSpan = if (targetCellWidth == 0) {
        (minWidth + cellWidth - 1) / cellWidth
    } else {
        targetCellWidth
    }

    return rowSpan to columnSpan
}

fun getSize(
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    targetCellWidth: Int,
    targetCellHeight: Int,
    minWidth: Int,
    minHeight: Int,
): Pair<Int, Int> {
    val cellWidth = gridWidth / columns

    val cellHeight = gridHeight / rows

    val width = if (targetCellWidth > 0) {
        targetCellWidth * cellWidth
    } else {
        minWidth
    }

    val height = if (targetCellHeight > 0) {
        targetCellHeight * cellHeight
    } else {
        minHeight
    }

    return width to height
}