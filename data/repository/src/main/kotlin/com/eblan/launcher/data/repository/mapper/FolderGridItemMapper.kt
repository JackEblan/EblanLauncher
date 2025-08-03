package com.eblan.launcher.data.repository.mapper

import com.eblan.launcher.data.room.entity.FolderGridItemEntity
import com.eblan.launcher.domain.model.FolderGridItem

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

