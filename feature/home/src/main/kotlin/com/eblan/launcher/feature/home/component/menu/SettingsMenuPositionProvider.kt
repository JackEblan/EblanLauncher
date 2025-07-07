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
        val (x, y) = calculateSettingsMenuCoordinates(
            x = x,
            y = y,
            windowSize = windowSize,
            popupContentSize = popupContentSize,
        )

        return IntOffset(x = x, y = y)
    }

    private fun calculateSettingsMenuCoordinates(
        x: Int,
        y: Int,
        windowSize: IntSize,
        popupContentSize: IntSize,
    ): Pair<Int, Int> {
        // Center the menu horizontally relative to the parent element.
        val childXInitial = x - popupContentSize.width / 2
        val childX = childXInitial.coerceIn(0, windowSize.width - popupContentSize.width)

        // Calculate possible vertical positions.
        val topPositionY = y - popupContentSize.height
        val bottomPositionY = y + popupContentSize.height

        // Choose the vertical position that keeps the menu on-screen.
        val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY
        val childY = childYInitial.coerceIn(0, windowSize.height - popupContentSize.height)

        return childX to childY
    }
}
