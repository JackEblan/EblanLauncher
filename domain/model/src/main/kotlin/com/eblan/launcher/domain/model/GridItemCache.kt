package com.eblan.launcher.domain.model

data class GridItemCache(
    val gridItemsCacheByPage: Map<Int, List<GridItem>>,
    val dockGridItemsCache: List<GridItem>,
)
