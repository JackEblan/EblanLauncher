package com.eblan.launcher.domain.model

data class IconPack(
    val packageName: String,
    val label: String,
    val icon: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IconPack

        if (packageName != other.packageName) return false
        if (label != other.label) return false
        if (!icon.contentEquals(other.icon)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        return result
    }
}