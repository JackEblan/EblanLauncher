package com.eblan.launcher.domain.model

data class EblanAppWidgetProviderInfo(
    val packageName: String,
    val componentName: String,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
)
