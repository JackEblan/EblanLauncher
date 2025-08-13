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
        gridItemSettings = folderGridItemEntity.gridItemSettings,
    )
}

internal fun FolderGridItemWrapperEntity.asFolderGridItemData(): GridItemData.Folder {
    val applicationInfos = applicationInfos.map { applicationInfoGridItemEntity ->
        applicationInfoGridItemEntity.asGridItem()
    }

    val widgets = widgets.map { widgetGridItemEntity ->
        widgetGridItemEntity.asGridItem()
    }

    val shortcutInfos = shortcutInfos.map { shortcutGridItemEntity ->
        shortcutGridItemEntity.asGridItem()
    }

    val folders = folders.map { folderGridItemEntity ->
        folderGridItemEntity.asGridItem()
    }

    return GridItemData.Folder(
        id = folderGridItemEntity.id,
        label = folderGridItemEntity.label,
        gridItems = applicationInfos + widgets + shortcutInfos + folders,
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
        gridItemSettings = gridItemSettings,
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
        gridItemSettings = gridItemSettings,
    )
}