package com.eblan.launcher.domain.model

sealed interface GridItemData {

    data class ApplicationInfo(
        val componentName: String?,
        val packageName: String,
        val icon: String?,
        val label: String?,
    ) : GridItemData

    data class Widget(
        val appWidgetId: Int,
        val componentName: String,
        val packageName: String,
        val configure: String?,
        val minWidth: Int,
        val minHeight: Int,
        val resizeMode: Int,
        val minResizeWidth: Int,
        val minResizeHeight: Int,
        val maxResizeWidth: Int,
        val maxResizeHeight: Int,
        val targetCellHeight: Int,
        val targetCellWidth: Int,
        val preview: String?,
    ) : GridItemData

    data class ShortcutInfo(
        val shortcutId: String,
        val packageName: String,
        val shortLabel: String,
        val longLabel: String,
        val icon: String?,
    ) : GridItemData

    data class Folder(
        val label: String,
        val gridItems: List<GridItem>,
    ) : GridItemData
}