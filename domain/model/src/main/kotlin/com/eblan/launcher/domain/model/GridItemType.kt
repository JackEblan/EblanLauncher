package com.eblan.launcher.domain.model

enum class GridItemType {
    Application, Widget
}

sealed interface GridItemTypeData {
    data class ApplicationInfo(
        val id: Int = 0,
        val packageName: String,
        val gridItemId: Int,
        val flags: Int,
        val icon: ByteArray?,
        val label: String,
    ) : GridItemTypeData {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ApplicationInfo

            return packageName == other.packageName
        }

        override fun hashCode(): Int {
            return packageName.hashCode()
        }
    }

    data class Widget(val label: String) : GridItemTypeData
}