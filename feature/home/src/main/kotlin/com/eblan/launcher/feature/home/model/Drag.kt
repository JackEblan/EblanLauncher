package com.eblan.launcher.feature.home.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

sealed interface Drag {
    data class Start(val offset: Offset, val size: IntSize) : Drag

    data object End : Drag

    data object Dragging : Drag

    data object Cancel : Drag

    data object None : Drag
}
