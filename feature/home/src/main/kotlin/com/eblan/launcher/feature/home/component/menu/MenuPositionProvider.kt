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
    private val screenWidth: Int,
    private val screenHeight: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val (x, y) = calculateMenuCoordinates(
            parentX = x,
            parentY = y,
            parentWidth = width,
            parentHeight = height,
            childWidth = popupContentSize.width,
            childHeight = popupContentSize.height,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )
        return IntOffset(x = x, y = y)
    }
}

private fun calculateMenuCoordinates(
    parentX: Int,
    parentY: Int,
    parentWidth: Int,
    parentHeight: Int,
    childWidth: Int,
    childHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
): Pair<Int, Int> {
    // Center the menu horizontally relative to the parent element.
    val parentCenterX = parentX + parentWidth / 2
    val childXInitial = parentCenterX - childWidth / 2
    val childX = childXInitial.coerceIn(0, screenWidth - childWidth)

    // Calculate possible vertical positions.
    val topPositionY = parentY - childHeight
    val bottomPositionY = parentY + parentHeight

    // Choose the vertical position that keeps the menu on-screen.
    val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY
    val childY = childYInitial.coerceIn(0, screenHeight - childHeight)

    return childX to childY
}
