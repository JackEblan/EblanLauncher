package com.eblan.launcher.domain.model

data class HomeData(
    val userData: UserData,
    val gridItems: List<GridItem>,
    val gridItemsByPage: Map<Int, List<GridItem>>,
    val dockGridItems: List<GridItem>,
    val hasShortcutHostPermission: Boolean,
    val textColor: Long,
)
