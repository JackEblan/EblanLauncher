package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem

data class MoveGridItem(val gridItem: GridItem, val rows: Int, val columns: Int)
