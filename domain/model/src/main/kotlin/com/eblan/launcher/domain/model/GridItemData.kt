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
        val minWidth: Int,
        val minHeight: Int,
    ) : GridItemData

    @Serializable
    data class WidgetAndroidTwelve(
        val appWidgetId: Int,
        val minWidth: Int,
        val minHeight: Int,
        val minResizeWidth: Int,
        val minResizeHeight: Int,
        val targetCellWidth: Int,
        val targetCellHeight: Int,
    ) : GridItemData
}