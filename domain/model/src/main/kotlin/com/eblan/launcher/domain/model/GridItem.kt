package com.eblan.launcher.domain.model

data class GridItem(
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val data: GridItemData,
    val associate: Associate,
    val override: Boolean,
    val iconSize: Int,
    val textColor: TextColor,
    val textSize: Int,
)

enum class Associate {
    Grid, Dock
}
