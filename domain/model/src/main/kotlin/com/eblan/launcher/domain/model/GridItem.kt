package com.eblan.launcher.domain.model

data class GridItem(
    val id: Int = 0,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
)
