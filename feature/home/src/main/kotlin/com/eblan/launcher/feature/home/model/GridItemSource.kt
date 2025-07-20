package com.eblan.launcher.feature.home.model

data class GridItemSource(val gridItemLayoutInfo: GridItemLayoutInfo, val type: Type) {
    enum class Type {
        New, Old, Pin,
    }
}