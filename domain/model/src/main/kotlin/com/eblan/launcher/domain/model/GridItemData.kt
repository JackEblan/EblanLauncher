package com.eblan.launcher.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed interface GridItemData {
    @Serializable
    data class ApplicationInfo(
        val gridItemId: Int,
        val packageName: String,
        @Transient val icon: ByteArray = ByteArray(0),
        val flags: Int,
        val label: String,
    ) : GridItemData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ApplicationInfo

            if (gridItemId != other.gridItemId) return false
            if (packageName != other.packageName) return false
            if (!icon.contentEquals(other.icon)) return false
            if (flags != other.flags) return false
            if (label != other.label) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gridItemId
            result = 31 * result + packageName.hashCode()
            result = 31 * result + icon.contentHashCode()
            result = 31 * result + flags
            result = 31 * result + label.hashCode()
            return result
        }
    }

    @Serializable
    data class Widget(val label: String) : GridItemData
}