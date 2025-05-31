package com.eblan.launcher.domain.model

data class GridItem(
    val id: String,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val dataId: String,
    val data: GridItemData,
    val associate: Associate,
)

enum class Associate {
    Grid, Dock
}
