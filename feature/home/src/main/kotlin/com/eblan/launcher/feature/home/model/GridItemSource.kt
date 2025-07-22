package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem

sealed interface GridItemSource {
    data class New(val gridItem: GridItem) : GridItemSource

    data class Pin(val gridItem: GridItem, val byteArray: ByteArray?) : GridItemSource {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Pin

            if (gridItem != other.gridItem) return false
            if (byteArray != null) {
                if (other.byteArray == null) return false
                if (!byteArray.contentEquals(other.byteArray)) return false
            } else if (other.byteArray != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gridItem.hashCode()
            result = 31 * result + (byteArray?.contentHashCode() ?: 0)
            return result
        }
    }
}