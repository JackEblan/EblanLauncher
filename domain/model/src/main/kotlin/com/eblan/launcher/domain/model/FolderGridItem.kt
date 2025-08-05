package com.eblan.launcher.domain.model

data class FolderGridItem(
    val id: String,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val label: String,
    val applicationInfos: List<ApplicationInfoGridItem>?,
    val widgets: List<WidgetGridItem>?,
    val shortcutInfos: List<ShortcutInfoGridItem>?,
)