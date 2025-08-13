package com.eblan.launcher.domain.model

data class FolderGridItem(
    val id: String,
    val folderId: String?,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val label: String,
    val override: Boolean,
    val iconSize: Int,
    val textColor: TextColor,
    val textSize: Int
)