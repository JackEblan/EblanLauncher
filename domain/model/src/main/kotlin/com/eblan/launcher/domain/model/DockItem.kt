package com.eblan.launcher.domain.model

data class DockItem(
    val id: String,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val data: GridItemData,
)
