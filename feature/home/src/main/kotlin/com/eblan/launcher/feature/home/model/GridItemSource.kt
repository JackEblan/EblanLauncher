package com.eblan.launcher.feature.home.model

import androidx.compose.ui.graphics.ImageBitmap
import com.eblan.launcher.domain.model.GridItemLayoutInfo

data class GridItemSource(val gridItemLayoutInfo: GridItemLayoutInfo, val type: Type, val imageBitmap: ImageBitmap?) {
    enum class Type {
        New, Old,
    }
}