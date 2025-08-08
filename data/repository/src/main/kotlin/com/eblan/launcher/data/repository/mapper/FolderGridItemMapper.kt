package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.data.room.entity.FolderGridItemWrapperEntity
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

internal fun FolderGridItemWrapperEntity.asGridItem(): GridItem {
    return GridItem(
        id = folderGridItemEntity.id,
        folderId = folderGridItemEntity.folderId,
        page = folderGridItemEntity.page,
        startRow = folderGridItemEntity.startRow,
        startColumn = folderGridItemEntity.startColumn,
        rowSpan = folderGridItemEntity.rowSpan,
        columnSpan = folderGridItemEntity.columnSpan,
        data = asFolderGridItemData(),
        associate = folderGridItemEntity.associate,
    )
}

internal fun FolderGridItemWrapperEntity.asFolderGridItemData(): GridItemData.Folder {
    val applicationInfoGridItems = applicationInfos?.map { applicationInfoGridItemEntity ->
        applicationInfoGridItemEntity.asGridItem()
    } ?: emptyList()

    val widgetGridItems = widgets?.map { widgetGridItemEntity ->
        widgetGridItemEntity.asGridItem()
    } ?: emptyList()

    val shortcutInfos = shortcutInfos?.map { shortcutGridItemEntity ->
        shortcutGridItemEntity.asGridItem()
    } ?: emptyList()

    val folders = folders?.map { folderGridItemEntity ->
        folderGridItemEntity.asGridItem()
    } ?: emptyList()

    return GridItemData.Folder(
        id = folderGridItemEntity.id,
        label = folderGridItemEntity.label,
        gridItems = applicationInfoGridItems + widgetGridItems + shortcutInfos + folders,
    )
}

@JvmName("FolderGridItemEntity")
internal fun FolderGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        data = GridItemData.Folder(
            id = id,
            label = label,
            gridItems = emptyList(),
        ),
        associate = associate,
    )
}

internal fun FolderGridItem.asEntity(): FolderGridItemEntity {
    return FolderGridItemEntity(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        label = label,
    )
}