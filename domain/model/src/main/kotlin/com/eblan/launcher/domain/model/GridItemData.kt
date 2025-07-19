package com.eblan.launcher.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed interface GridItemData {
    @Serializable
    data class ApplicationInfo(
        val componentName: String?,
        val packageName: String,
        val icon: String?,
        val label: String?,
    ) : GridItemData

    @Serializable
    data class Widget(
        val appWidgetId: Int,
        val componentName: String,
        val configure: String?,
        val width: Int,
        val height: Int,
        val resizeMode: Int,
        val minResizeWidth: Int,
        val minResizeHeight: Int,
        val maxResizeWidth: Int,
        val maxResizeHeight: Int,
        val preview: String?,
    ) : GridItemData

    @Serializable
    data class ShortcutInfo(
        val id: String,
        val packageName: String,
        val shortLabel: String,
        val longLabel: String,
        val icon: String?,
    ): GridItemData
}