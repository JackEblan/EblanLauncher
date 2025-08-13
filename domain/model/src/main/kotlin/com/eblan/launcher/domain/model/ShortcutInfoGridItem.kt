package com.eblan.launcher.domain.model

data class ShortcutInfoGridItem(
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val shortcutId: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: String?,
    val override: Boolean,
    val gridItemSettings: GridItemSettings,
)