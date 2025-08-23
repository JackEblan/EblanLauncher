package com.eblan.launcher.feature.home.component.gestures

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
import com.eblan.launcher.feature.home.component.gestures.LongPressResult.Canceled
import kotlinx.coroutines.coroutineScope

/**
 * We don't consume the long press so touch events propagate to the parent
 */
suspend fun PointerInputScope.detectTapGesturesUnConsume(
    requireUnconsumed: Boolean = true,
    onLongPress: ((Offset) -> Unit)? = null,
    onTap: ((Offset) -> Unit)? = null,
) = coroutineScope {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = requireUnconsumed)
        down.consume()

        val upOrCancel = if (onLongPress == null) {
            waitForUpOrCancellation()
        } else {
            when (val longPressResult = waitForLongPress()) {
                LongPressResult.Success -> {
                    onLongPress.invoke(down.position)
                    return@awaitEachGesture
                }

                is LongPressResult.Released -> longPressResult.finalUpChange
                is Canceled -> null
            }
        }

        if (upOrCancel != null) {
            upOrCancel.consume()

            onTap?.invoke(upOrCancel.position)
        }
    }
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