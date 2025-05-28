package com.eblan.launcher.domain.model

data class EblanAppWidgetProviderInfo(
    val className: String,
    val componentName: String,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val preview: String?,
    val eblanApplicationInfo: EblanApplicationInfo,
)