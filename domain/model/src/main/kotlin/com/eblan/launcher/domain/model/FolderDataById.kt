package com.eblan.launcher.domain.model

data class FolderDataById(
    val id: String,
    val label: String,
    val gridItems: List<GridItem>,
    val gridItemsByPage: Map<Int, List<GridItem>>,
    val pageCount: Int,
)