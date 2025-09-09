package com.eblan.launcher.feature.home.component.menu

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

class SettingsMenuPositionProvider(
    private val x: Int,
    private val y: Int,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val x = x.coerceIn(0, windowSize.width)

        val y = y.coerceIn(0, windowSize.height)

        return IntOffset(x = x, y = y)
    }
}
