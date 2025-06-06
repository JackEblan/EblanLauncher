package com.eblan.launcher.domain.model

data class AppWidgetManagerAppWidgetProviderInfo(
    val className: String,
    val packageName: String,
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
    val preview: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppWidgetManagerAppWidgetProviderInfo

        if (className != other.className) return false
        if (packageName != other.packageName) return false
        if (componentName != other.componentName) return false
        if (targetCellWidth != other.targetCellWidth) return false
        if (targetCellHeight != other.targetCellHeight) return false
        if (minWidth != other.minWidth) return false
        if (minHeight != other.minHeight) return false
        if (resizeMode != other.resizeMode) return false
        if (minResizeWidth != other.minResizeWidth) return false
        if (minResizeHeight != other.minResizeHeight) return false
        if (maxResizeWidth != other.maxResizeWidth) return false
        if (maxResizeHeight != other.maxResizeHeight) return false
        if (preview != null) {
            if (other.preview == null) return false
            if (!preview.contentEquals(other.preview)) return false
        } else if (other.preview != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = className.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + componentName.hashCode()
        result = 31 * result + targetCellWidth
        result = 31 * result + targetCellHeight
        result = 31 * result + minWidth
        result = 31 * result + minHeight
        result = 31 * result + resizeMode
        result = 31 * result + minResizeWidth
        result = 31 * result + minResizeHeight
        result = 31 * result + maxResizeWidth
        result = 31 * result + maxResizeHeight
        result = 31 * result + (preview?.contentHashCode() ?: 0)
        return result
    }
}