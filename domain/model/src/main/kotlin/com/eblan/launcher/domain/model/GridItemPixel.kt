package com.eblan.launcher.domain.model

data class GridItemPixel(
    val gridItem: GridItem,
    val boundingBox: BoundingBox,
    val coordinates: Coordinates,
    val data: GridItemData?,
)