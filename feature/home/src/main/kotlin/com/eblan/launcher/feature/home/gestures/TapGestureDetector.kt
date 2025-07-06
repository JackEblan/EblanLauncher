package com.eblan.launcher.feature.home.gestures

import android.view.MotionEvent
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.eblan.launcher.feature.home.gestures.LongPressResult.Canceled
import kotlinx.coroutines.coroutineScope

/**
 * We don't consume the long press so touch events propagate to the parent
 */
suspend fun PointerInputScope.detectTapGesturesUnConsume(
    requireUnconsumed: Boolean = true,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = coroutineScope {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = requireUnconsumed)
        down.consume()
        val upOrCancel: PointerInputChange?

        // wait for first tap up or long press
        if (onLongPress == null) {
            upOrCancel = waitForUpOrCancellation()
        } else {
            upOrCancel =
                when (val longPressResult = waitForLongPress()) {
                    LongPressResult.Success -> {
                        onLongPress.invoke(down.position)
                        // consumeUntilUp()
                        // End the current gesture
                        return@awaitEachGesture
                    }

                    is LongPressResult.Released -> longPressResult.finalUpChange
                    is Canceled -> null
                }
        }

        if (upOrCancel != null) {
            upOrCancel.consume()
            // tap was successful.
            if (onDoubleTap == null) {
                onTap?.invoke(upOrCancel.position) // no need to check for double-tap.
            } else {
                // check for second tap
                val secondDown = awaitSecondDown(upOrCancel)

                if (secondDown == null) {
                    onTap?.invoke(upOrCancel.position) // no valid second tap started
                } else {
                    // Might have a long second press as the second tap
                    val secondUp =
                        if (onLongPress == null) {
                            waitForUpOrCancellation()
                        } else {
                            when (val longPressResult = waitForLongPress()) {
                                LongPressResult.Success -> {
                                    // The first tap was valid, but the second tap is a long press -
                                    // we
                                    // intentionally do not invoke onClick() for the first tap,
                                    // since the 'main'
                                    // gesture here is a long press, which canceled the double tap
                                    // / tap.

                                    // notify for the long press
                                    onLongPress.invoke(secondDown.position)
                                    consumeUntilUp()

                                    return@awaitEachGesture
                                }

                                is LongPressResult.Released -> longPressResult.finalUpChange
                                is Canceled -> null
                            }
                        }
                    if (secondUp != null) {
                        secondUp.consume()
                        onDoubleTap(secondUp.position)
                    } else {
                        onTap?.invoke(upOrCancel.position)
                    }
                }
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitSecondDown(
    firstUp: PointerInputChange,
): PointerInputChange? =
    withTimeoutOrNull(viewConfiguration.doubleTapTimeoutMillis) {
        val minUptime = firstUp.uptimeMillis + viewConfiguration.doubleTapMinTimeMillis
        var change: PointerInputChange
        // The second tap doesn't count if it happens before DoubleTapMinTime of the first tap
        do {
            change = awaitFirstDown()
        } while (change.uptimeMillis < minUptime)
        change
    }

private suspend fun AwaitPointerEventScope.consumeUntilUp() {
    do {
        val event = awaitPointerEvent()
        event.changes.fastForEach { it.consume() }
    } while (event.changes.fastAny { it.pressed })
}

private suspend fun AwaitPointerEventScope.waitForLongPress(
    pass: PointerEventPass = PointerEventPass.Main,
): LongPressResult {
    var result: LongPressResult = Canceled
    try {
        withTimeout(viewConfiguration.longPressTimeoutMillis) {
            while (true) {
                val event = awaitPointerEvent(pass)
                if (event.changes.fastAll { it.changedToUp() }) {
                    // All pointers are up
                    result = LongPressResult.Released(event.changes[0])
                    break
                }

                if (event.classification == MotionEvent.CLASSIFICATION_DEEP_PRESS) {
                    result = LongPressResult.Success
                    break
                }

                if (
                    event.changes.fastAny {
                        it.isConsumed || it.isOutOfBounds(size, extendedTouchPadding)
                    }
                ) {
                    result = Canceled
                    break
                }

                // Check for cancel by position consumption. We can look on the Final pass of the
                // existing pointer event because it comes after the pass we checked above.
                val consumeCheck = awaitPointerEvent(PointerEventPass.Final)
                if (consumeCheck.changes.fastAny { it.isConsumed }) {
                    result = Canceled
                    break
                }
            }
        }
    } catch (_: PointerEventTimeoutCancellationException) {
        return LongPressResult.Success
    }
    return result
}

private sealed class LongPressResult {
    /** Long press was triggered */
    data object Success : LongPressResult()

    /** All pointers were released without long press being triggered */
    class Released(val finalUpChange: PointerInputChange) : LongPressResult()

    /** The gesture was canceled */
    data object Canceled : LongPressResult()
}