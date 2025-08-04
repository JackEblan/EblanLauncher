package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.ApplicationInfoGridItemEntity
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData


internal fun ApplicationInfoGridItemEntity.asGridItem(): GridItem {
    return GridItem(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        data = GridItemData.ApplicationInfo(
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        ),
        associate = associate,
        zIndex = zIndex,
    )
}

internal fun ApplicationInfoGridItemEntity.asModel(): ApplicationInfoGridItem {
    return ApplicationInfoGridItem(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        zIndex = zIndex,
    )
}

internal fun ApplicationInfoGridItem.asEntity(): ApplicationInfoGridItemEntity {
    return ApplicationInfoGridItemEntity(
        id = id,
        page = page,
        startRow = startRow,
        startColumn = startColumn,
        rowSpan = rowSpan,
        columnSpan = columnSpan,
        associate = associate,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        zIndex = zIndex,
    )
}