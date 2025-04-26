package com.eblan.launcher.domain.model

data class GridItemsByPage(
    val userData: UserData,
    val gridItems: Map<Int, List<GridItem>>,
    val dockItems: List<DockItem>,
)
