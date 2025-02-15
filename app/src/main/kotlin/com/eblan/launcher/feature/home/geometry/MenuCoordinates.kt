package com.eblan.launcher.feature.home.geometry

data class MenuCoordinates(val x: Int, val y: Int)

fun calculateMenuCoordinates(
    parentX: Int,
    parentY: Int,
    parentWidth: Int,
    parentHeight: Int,
    childWidth: Int,
    childHeight: Int,
    screenWidth: Int,
    screenHeight: Int,
    margin: Int
): MenuCoordinates {
    val parentCenterX = parentX + parentWidth / 2

    val childXInitial = parentCenterX - childWidth / 2

    val childX = childXInitial.coerceIn(0, screenWidth - childWidth)

    val topPositionY = parentY - margin - childHeight

    val bottomPositionY = parentY + parentHeight + margin

    val childYInitial = if (topPositionY < 0) bottomPositionY else topPositionY

    val childY = childYInitial.coerceIn(0, screenHeight - childHeight)

    return MenuCoordinates(x = childX, y = childY)
}