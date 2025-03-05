package com.eblan.launcher.domain.geometry

import com.eblan.launcher.domain.model.MenuCoordinates

/**
 * Calculates a great spot to show your menu relative to a parent element.
 *
 * Centers the menu horizontally on the parent element and then decides whether
 * to display it above or below the parent based on available space. It also makes sure the menu
 * doesn't go off the screen.
 *
 * @param parentX The x-coordinate of the parent element.
 * @param parentY The y-coordinate of the parent element.
 * @param parentWidth The width of the parent element.
 * @param parentHeight The height of the parent element.
 * @param childWidth The width of the menu.
 * @param childHeight The height of the menu.
 * @param screenWidth The overall screen width.
 * @param screenHeight The overall screen height.
 * @return A [MenuCoordinates] object with the x and y coordinates for where the menu should appear.
 */
fun calculateMenuCoordinates(
    parentX: Int,
    parentY: Int,
    parentWidth: Int,
    parentHeight: Int,
    childWidth: Int,
    childHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
): MenuCoordinates {
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

    return MenuCoordinates(x = childX, y = childY)
}
