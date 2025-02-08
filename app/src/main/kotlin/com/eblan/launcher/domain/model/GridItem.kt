package com.eblan.launcher.domain.model

data class GridItem(
    val page: Int, val id: Int, val cells: List<GridCell>
)