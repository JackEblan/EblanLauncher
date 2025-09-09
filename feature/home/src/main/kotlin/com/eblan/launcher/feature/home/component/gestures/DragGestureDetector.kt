package com.eblan.launcher.feature.home.component.gestures

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import kotlin.math.absoluteValue
import kotlin.math.sign

suspend fun PointerInputScope.detectVerticalDragGestures(
    requireUnconsumed: Boolean,
    onDragStart: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onVerticalDrag: (change: PointerInputChange, dragAmount: Float) -> Unit
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = requireUnconsumed)
        var overSlop = 0f
        val drag =
            awaitVerticalPointerSlopOrCancellation(down.id, down.type) { change, over ->
                change.consume()
                overSlop = over
            }
        if (drag != null) {
            onDragStart.invoke(drag.position)
            onVerticalDrag.invoke(drag, overSlop)
            if (
                verticalDrag(drag.id) {
                    onVerticalDrag(it, it.positionChange().y)
                    it.consume()
                }
            ) {
                onDragEnd()
            } else {
                onDragCancel()
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitVerticalPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    onTouchSlopReached: (change: PointerInputChange, overSlop: Float) -> Unit
) =
    awaitPointerSlopOrCancellation(
        pointerId = pointerId,
        pointerType = pointerType,
        onPointerSlopReached = { change, overSlop -> onTouchSlopReached(change, overSlop.y) },
        orientation = Orientation.Vertical
    )

private suspend inline fun AwaitPointerEventScope.awaitPointerSlopOrCancellation(
    pointerId: PointerId,
    pointerType: PointerType,
    orientation: Orientation?,
    initialPositionChange: Offset = Offset.Zero,
    onPointerSlopReached: (PointerInputChange, Offset) -> Unit,
): PointerInputChange? {
    if (currentEvent.isPointerUp(pointerId)) {
        return null // The pointer has already been lifted, so the gesture is canceled
    }

    val touchSlop = viewConfiguration.pointerSlop(pointerType)
    var pointer: PointerId = pointerId
    val touchSlopDetector = TouchSlopDetector(orientation, initialPositionChange)
    while (true) {
        val event = awaitPointerEvent()
        val dragEvent = event.changes.fastFirstOrNull { it.id == pointer } ?: return null
        if (dragEvent.isConsumed) {
            return null
        } else if (dragEvent.changedToUpIgnoreConsumed()) {
            val otherDown = event.changes.fastFirstOrNull { it.pressed }
            if (otherDown == null) {
                // This is the last "up"
                return null
            } else {
                pointer = otherDown.id
            }
        } else {
            val postSlopOffset = touchSlopDetector.addPointerInputChange(dragEvent, touchSlop)
            if (postSlopOffset.isSpecified) {
                onPointerSlopReached(dragEvent, postSlopOffset)
                if (dragEvent.isConsumed) {
                    return dragEvent
                } else {
                    touchSlopDetector.reset()
                }
            } else {
                // verify that nothing else consumed the drag event
                awaitPointerEvent(PointerEventPass.Final)
                if (dragEvent.isConsumed) {
                    return null
                }
            }
        }
    }
}

private fun PointerEvent.isPointerUp(pointerId: PointerId): Boolean =
    changes.fastFirstOrNull { it.id == pointerId }?.pressed != true

// This value was determined using experiments and common sense.
// We can't use zero slop, because some hypothetical desktop/mobile devices can send
// pointer events with a very high precision (but I haven't encountered any that send
// events with less than 1px precision)
private val mouseSlop = 0.125.dp
private val defaultTouchSlop = 18.dp // The default touch slop on Android devices
private val mouseToTouchSlopRatio = mouseSlop / defaultTouchSlop

// TODO(demin): consider this as part of ViewConfiguration class after we make *PointerSlop*
//  functions public (see the comment at the top of the file).
//  After it will be a public API, we should get rid of `touchSlop / 144` and return absolute
//  value 0.125.dp.toPx(). It is not possible right now, because we can't access density.
private fun ViewConfiguration.pointerSlop(pointerType: PointerType): Float {
    return when (pointerType) {
        PointerType.Mouse -> touchSlop * mouseToTouchSlopRatio
        else -> touchSlop
    }
}

private class TouchSlopDetector(
    val orientation: Orientation? = null,
    initialPositionChange: Offset = Offset.Zero
) {

    fun Offset.mainAxis() = if (orientation == Orientation.Horizontal) x else y

    fun Offset.crossAxis() = if (orientation == Orientation.Horizontal) y else x

    /** The accumulation of drag deltas in this detector. */
    private var totalPositionChange: Offset = initialPositionChange

    /**
     * Adds [dragEvent] to this detector. If the accumulated position changes crosses the touch slop
     * provided by [touchSlop], this method will return the post slop offset, that is the total
     * accumulated delta change minus the touch slop value, otherwise this should return null.
     */
    fun addPointerInputChange(dragEvent: PointerInputChange, touchSlop: Float): Offset {
        val currentPosition = dragEvent.position
        val previousPosition = dragEvent.previousPosition
        val positionChange = currentPosition - previousPosition
        totalPositionChange += positionChange

        val inDirection =
            if (orientation == null) {
                totalPositionChange.getDistance()
            } else {
                totalPositionChange.mainAxis().absoluteValue
            }

        val hasCrossedSlop = inDirection >= touchSlop

        return if (hasCrossedSlop) {
            calculatePostSlopOffset(touchSlop)
        } else {
            Offset.Unspecified
        }
    }

    /** Resets the accumulator associated with this detector. */
    fun reset() {
        totalPositionChange = Offset.Zero
    }

    private fun calculatePostSlopOffset(touchSlop: Float): Offset {
        return if (orientation == null) {
            val touchSlopOffset =
                totalPositionChange / totalPositionChange.getDistance() * touchSlop
            // update postSlopOffset
            totalPositionChange - touchSlopOffset
        } else {
            val finalMainAxisChange =
                totalPositionChange.mainAxis() - (sign(totalPositionChange.mainAxis()) * touchSlop)
            val finalCrossAxisChange = totalPositionChange.crossAxis()
            if (orientation == Orientation.Horizontal) {
                Offset(finalMainAxisChange, finalCrossAxisChange)
            } else {
                Offset(finalCrossAxisChange, finalMainAxisChange)
            }
        }
    }
}
