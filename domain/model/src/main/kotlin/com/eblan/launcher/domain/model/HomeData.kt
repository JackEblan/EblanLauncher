package com.eblan.launcher.domain.model

data class HomeData(
    val userData: UserData,
    val gridItems: Map<Int, List<GridItem>>,
    val dockGridItems: List<GridItem>,
    val pageItems: List<PageItem>,
    val hasShortcutHostPermission: Boolean,
    val textColor: Long,
)
