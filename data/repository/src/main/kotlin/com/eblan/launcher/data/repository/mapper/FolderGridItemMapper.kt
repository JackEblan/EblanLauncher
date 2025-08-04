package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

internal fun FolderGridItemEntity.asModel(): FolderGridItem {
    return FolderGridItem(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        label = label,
    )
}

internal fun FolderGridItem.asEntity(): FolderGridItemEntity {
    return FolderGridItemEntity(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        label = label,
    )
}

internal fun FolderGridItemWrapperEntity.asModel(): GridItem {
    val applicationInfoGridItems = applicationInfos?.map { applicationInfoGridItemEntity ->
        applicationInfoGridItemEntity.asGridItem()
    } ?: emptyList()

    val widgetGridItems = widgets?.map { widgetGridItemEntity ->
        widgetGridItemEntity.asGridItem()
    } ?: emptyList()

    val shortcutInfos = shortcutInfos?.map { shortcutGridItemEntity ->
        shortcutGridItemEntity.asGridItem()
    } ?: emptyList()

    val data = GridItemData.Folder(
        label = folderGridItemEntity.label,
        gridItems = applicationInfoGridItems + widgetGridItems + shortcutInfos,
    )

    return GridItem(
        id = folderGridItemEntity.id,
        folderId = null,
        page = folderGridItemEntity.page,
        startRow = folderGridItemEntity.startRow,
        startColumn = folderGridItemEntity.startColumn,
        rowSpan = folderGridItemEntity.rowSpan,
        columnSpan = folderGridItemEntity.columnSpan,
        data = data,
        associate = folderGridItemEntity.associate,
    )
}

