package com.eblan.launcher.domain.model

data class LauncherAppsShortcutInfo(
    val id: String,
    val packageName: String,
    val shortLabel: String,
    val longLabel: String,
    val icon: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LauncherAppsShortcutInfo

        if (id != other.id) return false
        if (packageName != other.packageName) return false
        if (shortLabel != other.shortLabel) return false
        if (longLabel != other.longLabel) return false
        if (!icon.contentEquals(other.icon)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + shortLabel.hashCode()
        result = 31 * result + longLabel.hashCode()
        result = 31 * result + icon.contentHashCode()
        return result
    }
}
