package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem

data class GridItemLayoutInfo(
    val gridItem: GridItem,
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
    val screenWidth: Int,
    val screenHeight: Int,
)