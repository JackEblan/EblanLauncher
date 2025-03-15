package com.eblan.launcher.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface GridItemData {
    @Serializable
    data class ApplicationInfo(
        val packageName: String,
        val icon: String?,
        val label: String,
    ) : GridItemData

    @Serializable
    data class Widget(
        val appWidgetId: Int,
        val componentName: String,
        val width: Int,
        val height: Int,
        val resizeMode: Int,
        val minResizeWidth: Int,
        val minResizeHeight: Int,
        val maxResizeWidth: Int,
        val maxResizeHeight: Int,
    ) : GridItemData
}