package com.eblan.launcher.domain.model

data class GridItem(
    val id: Int = 0,
    val page: Int, val cells: List<GridCell>,
)