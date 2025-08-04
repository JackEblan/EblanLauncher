package com.eblan.launcher.domain.model

data class ApplicationInfoGridItem(
    val id: String,
    val page: Int,
    val startRow: Int,
    val startColumn: Int,
    val rowSpan: Int,
    val columnSpan: Int,
    val associate: Associate,
    val componentName: String?,
    val packageName: String,
    val icon: String?,
    val label: String?,
    val zIndex: Int,
)