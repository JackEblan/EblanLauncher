package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItemLayoutInfo

data class GridItemSource(val gridItemLayoutInfo: GridItemLayoutInfo, val type: Type) {
    enum class Type {
        New, Old,
    }
}