package com.eblan.launcher.feature.home.geometry

import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates

/**
 * Represents a bounding box that can be resized.
 *
 * @property x The x-coordinate (top-left) of the bounding box.
 * @property y The y-coordinate (top-left) of the bounding box.
 * @property width The width of the bounding box.
 * @property height The height of the bounding box.
 */
data class ResizableBoundingBox(val x: Int, val y: Int, val width: Int, val height: Int)

/**
 * Calculates a new resizable bounding box based on the provided [coordinates] and [boundingBox].
 *
 * Expands the original [boundingBox] by adding a [margin] to both its width and height,
 * ensuring that the new dimensions are at least as big as the margin. It then centers the new bounding
 * box around the center of the original one.
 *
 * @param coordinates The top-left coordinates of the original bounding box.
 * @param boundingBox The original bounding box that you want to resize.
 * @param margin The extra space to add to the bounding box dimensions. Defaults to 100.
 * @return A [ResizableBoundingBox] with updated position and size.
 */
fun calculateResizableBoundingBox(
    coordinates: Coordinates,
    boundingBox: BoundingBox,
    margin: Int = 100
): ResizableBoundingBox {
    // Increase the size of the bounding box by the margin, but ensure it's at least the margin in size.
    val newWidth = (boundingBox.width + margin).coerceAtLeast(margin)
    val newHeight = (boundingBox.height + margin).coerceAtLeast(margin)

    // Calculate the center of the original bounding box.
    val centerX = coordinates.x + boundingBox.width / 2
    val centerY = coordinates.y + boundingBox.height / 2

    // Position the new bounding box so that it's centered over the original.
    val newX = centerX - newWidth / 2
    val newY = centerY - newHeight / 2

    return ResizableBoundingBox(x = newX, y = newY, width = newWidth, height = newHeight)
}
