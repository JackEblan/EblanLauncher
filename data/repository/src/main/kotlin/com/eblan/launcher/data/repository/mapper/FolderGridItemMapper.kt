package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.data.room.entity.WidgetGridItemEntity
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem

internal fun FolderGridItemEntity.asModel(): FolderGridItem {
    return FolderGridItem(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
    )
}

internal fun List<ApplicationInfoGridItemEntity>?.asModels(): List<GridItem> {
    return this?.map { applicationInfoGridItemEntity ->
        applicationInfoGridItemEntity.asGridItem()
    } ?: emptyList()
}

internal fun List<WidgetGridItemEntity>?.asModels(): List<GridItem> {
    return this?.map { widgetGridItemEntity ->
        widgetGridItemEntity.asGridItem()
    } ?: emptyList()
}

internal fun List<ShortcutInfoGridItemEntity>?.asModels(): List<GridItem> {
    return this?.map { shortcutGridItemEntity ->
        shortcutGridItemEntity.asGridItem()
    } ?: emptyList()
}