package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.WidgetGridItem


internal fun WidgetGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        data = GridItemData.Widget(
            appWidgetId = appWidgetId,
            componentName = componentName,
            packageName = packageName,
            configure = configure,
            minWidth = minWidth,
            minHeight = minHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            targetCellHeight = targetCellHeight,
            targetCellWidth = targetCellWidth,
            preview = preview,
        ),
        associate = associate,
        gridItemSettings = gridItemSettings,
    )
}

internal fun WidgetGridItemEntity.asModel(): WidgetGridItem {
    return WidgetGridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        appWidgetId = appWidgetId,
        packageName = packageName,
        componentName = componentName,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
        gridItemSettings = gridItemSettings,
    )
}

internal fun WidgetGridItem.asEntity(): WidgetGridItemEntity {
    return WidgetGridItemEntity(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        appWidgetId = appWidgetId,
        packageName = packageName,
        componentName = componentName,
        configure = configure,
        minWidth = minWidth,
        minHeight = minHeight,
        resizeMode = resizeMode,
        minResizeWidth = minResizeWidth,
        minResizeHeight = minResizeHeight,
        maxResizeWidth = maxResizeWidth,
        maxResizeHeight = maxResizeHeight,
        targetCellHeight = targetCellHeight,
        targetCellWidth = targetCellWidth,
        preview = preview,
        gridItemSettings = gridItemSettings,
    )
}