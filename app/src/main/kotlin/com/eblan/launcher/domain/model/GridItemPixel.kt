package com.eblan.launcher.domain.model

import com.eblan.launcher.domain.grid.Coordinates

data class GridItemPixel(
    val gridItem: GridItem,
    val boundingBox: BoundingBox,
    val coordinates: Coordinates,
)