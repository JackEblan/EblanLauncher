package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem

data class GridItemSource(val gridItem: GridItem, val type: Type) {
    enum class Type {
        New, Pin,
    }
}