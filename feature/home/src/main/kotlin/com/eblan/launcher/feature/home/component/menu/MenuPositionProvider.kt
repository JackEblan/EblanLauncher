package com.eblan.launcher.feature.home.component.menu

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider
import com.eblan.launcher.domain.geometry.calculateMenuCoordinates

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
        val coordinates = calculateMenuCoordinates(
            parentX = x,
            parentY = y,
            parentWidth = width,
            parentHeight = height,
            childWidth = popupContentSize.width,
            childHeight = popupContentSize.height,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )
        return IntOffset(coordinates.x, coordinates.y)
    }
}