package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

fun getShortcutGridItem(
    page: Int,
    id: String,
    packageName: String,
    shortLabel: String,
    longLabel: String,
    icon: String?,
): GridItem {
    val data = GridItemData.ShortcutInfo(
        id = id,
        packageName = packageName,
        shortLabel = shortLabel,
        longLabel = longLabel,
        icon = icon,
    )

    return GridItem(
        id = 0,
        page = page,
        startRow = 0,
        startColumn = 0,
        rowSpan = 1,
        columnSpan = 1,
        dataId = data.packageName,
        data = data,
        associate = Associate.Grid,
    )
}