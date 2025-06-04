package com.eblan.launcher.domain.model

data class EblanLauncherActivityInfo(
    val componentName: String,
    val packageName: String,
    val icon: ByteArray?,
    val label: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EblanLauncherActivityInfo

        if (packageName != other.packageName) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        if (label != other.label) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + label.hashCode()
        return result
    }
}