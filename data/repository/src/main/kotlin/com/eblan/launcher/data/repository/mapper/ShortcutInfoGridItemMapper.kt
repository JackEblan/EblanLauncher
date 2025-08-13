package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.ShortcutInfoGridItemEntity
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem


internal fun ShortcutInfoGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        data = GridItemData.ShortcutInfo(
            shortcutId = shortcutId,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
        ),
        associate = associate,
        gridItemSettings = gridItemSettings,
    )
}

internal fun ShortcutInfoGridItemEntity.asModel(): ShortcutInfoGridItem {
    return ShortcutInfoGridItem(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        shortcutId = shortcutId,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        gridItemSettings = gridItemSettings,
    )
}

internal fun ShortcutInfoGridItem.asEntity(): ShortcutInfoGridItemEntity {
    return ShortcutInfoGridItemEntity(
        id = id,
        folderId = folderId,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        shortcutId = shortcutId,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
        gridItemSettings = gridItemSettings,
    )
}