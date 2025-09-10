package com.eblan.launcher.feature.home.component.menu

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

class MenuPositionProvider(
    private val x: Int,
    private val y: Int,
    private val width: Int,
    private val height: Int,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val (x, y) = calculateMenuCoordinates(
            x = x,
            y = y,
            width = width,
            height = height,
            windowSize = windowSize,
            popupContentSize = popupContentSize,
        )

        return IntOffset(x = x, y = y)
    }

    private fun calculateMenuCoordinates(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        windowSize: IntSize,
        popupContentSize: IntSize,
    ): Pair<Int, Int> {
        val parentCenterX = x + width / 2
        val childXInitial = parentCenterX - popupContentSize.width / 2
        val childX = childXInitial.coerceIn(0, windowSize.width - popupContentSize.width)

        val topPositionY = y - popupContentSize.height
        val bottomPositionY = y + (popupContentSize.height + height)

        val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY
        val childY = childYInitial.coerceIn(0, windowSize.height - popupContentSize.height)

        return childX to childY
    }
}