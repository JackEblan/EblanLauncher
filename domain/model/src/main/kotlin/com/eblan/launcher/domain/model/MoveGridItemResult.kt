package com.eblan.launcher.domain.model

data class MoveGridItemResult(
    val gridItems: List<GridItem>?,
    val movingGridItem: GridItem,
    val conflictingGridItem: GridItem?,
)
