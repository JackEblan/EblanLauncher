package com.eblan.launcher.feature.home.geometry

import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates

data class ResizableBoundingBox(val x: Int, val y: Int, val width: Int, val height: Int)

fun calculateResizableBoundingBox(
    coordinates: Coordinates, boundingBox: BoundingBox, margin: Int = 100
): ResizableBoundingBox {
    val newWidth = (boundingBox.width + margin).coerceAtLeast(margin)
    val newHeight = (boundingBox.height + margin).coerceAtLeast(margin)

    val centerX = coordinates.x + boundingBox.width / 2
    val centerY = coordinates.y + boundingBox.height / 2

    val newX = centerX - newWidth / 2
    val newY = centerY - newHeight / 2

    return ResizableBoundingBox(x = newX, y = newY, width = newWidth, height = newHeight)
}
