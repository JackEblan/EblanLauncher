package com.eblan.launcher.domain.model

data class GridItemOverlay(
    val gridItem: GridItem,
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
    val screenWidth: Int,
    val screenHeight: Int,
)