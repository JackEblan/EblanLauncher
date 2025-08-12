package com.eblan.launcher.domain.model

data class HomeSettings(
    val rows: Int,
    val columns: Int,
    val pageCount: Int,
    val infiniteScroll: Boolean,
    val dockRows: Int,
    val dockColumns: Int,
    val dockHeight: Int,
    val initialPage: Int,
    val wallpaperScroll: Boolean,
    val folderRows: Int,
    val folderColumns: Int,
    val gridItemSettings: GridItemSettings,
)
