package com.eblan.launcher.feature.home.component.gestures

import android.view.MotionEvent
import androidx.compose.foundation.ComposeFoundationFlags.isDetectTapGesturesImmediateCoroutineDispatchEnabled
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.PressGestureScope
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.util.fastAll
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.eblan.launcher.feature.home.component.gestures.LongPressResult.Canceled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

private val NoPressGesture: suspend PressGestureScope.(Offset) -> Unit = {}

@ExperimentalFoundationApi
private val coroutineStartForCurrentDispatchBehavior
    get() =
        if (isDetectTapGesturesImmediateCoroutineDispatchEnabled) {
            CoroutineStart.UNDISPATCHED
        } else {
            CoroutineStart.DEFAULT
        }

/**
 * We don't consume the long press so touch events propagate to the parent
 */
@OptIn(ExperimentalFoundationApi::class)
suspend fun PointerInputScope.detectTapGesturesUnConsume(
    requireUnconsumed: Boolean = true,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onLongPress: ((Offset) -> Unit)? = null,
    onPress: suspend PressGestureScope.(Offset) -> Unit = NoPressGesture,
    onTap: ((Offset) -> Unit)? = null,
) = coroutineScope {
    // special signal to indicate to the sending side that it shouldn't intercept and consume
    // cancel/up events as we're only require down events
    val pressScope = PressGestureScopeImpl(this@detectTapGesturesUnConsume)
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = requireUnconsumed)
        down.consume()
        var resetJob =
            launch(start = coroutineStartForCurrentDispatchBehavior) { pressScope.reset() }
        if (onPress !== NoPressGesture)
            launchAwaitingReset(resetJob) { pressScope.onPress(down.position) }
        val upOrCancel: PointerInputChange?
        val cancelOrReleaseJob: Job?

        // wait for first tap up or long press
        if (onLongPress == null) {
            upOrCancel = waitForUpOrCancellation()
        } else {
            upOrCancel =
                when (val longPressResult = waitForLongPress()) {
                    LongPressResult.Success -> {
                        onLongPress.invoke(down.position)
                        // consumeUntilUp()
                        launchAwaitingReset(resetJob) { pressScope.release() }
                        // End the current gesture
                        return@awaitEachGesture
                    }

                    is LongPressResult.Released -> longPressResult.finalUpChange
                    is Canceled -> null
                }
        }

        if (upOrCancel == null) {
            cancelOrReleaseJob =
                launchAwaitingReset(resetJob) {
                    // tap-up was canceled
                    pressScope.cancel()
                }
        } else {
            upOrCancel.consume()
            cancelOrReleaseJob = launchAwaitingReset(resetJob) { pressScope.release() }
        }

        if (upOrCancel != null) {
            // tap was successful.
            if (onDoubleTap == null) {
                onTap?.invoke(upOrCancel.position) // no need to check for double-tap.
            } else {
                // check for second tap
                val secondDown = awaitSecondDown(upOrCancel)

                if (secondDown == null) {
                    onTap?.invoke(upOrCancel.position) // no valid second tap started
                } else {
                    // Second tap down detected
                    resetJob =
                        launch(start = coroutineStartForCurrentDispatchBehavior) {
                            cancelOrReleaseJob.join()
                            pressScope.reset()
                        }
                    if (onPress !== NoPressGesture) {
                        launchAwaitingReset(resetJob) { pressScope.onPress(secondDown.position) }
                    }

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

                                    launchAwaitingReset(resetJob) { pressScope.release() }
                                    return@awaitEachGesture
                                }

                                is LongPressResult.Released -> longPressResult.finalUpChange
                                is Canceled -> null
                            }
                        }
                    if (secondUp != null) {
                        secondUp.consume()
                        launchAwaitingReset(resetJob) { pressScope.release() }
                        onDoubleTap(secondUp.position)
                    } else {
                        launchAwaitingReset(resetJob) { pressScope.cancel() }
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

@OptIn(ExperimentalFoundationApi::class)
private fun CoroutineScope.launchAwaitingReset(
    resetJob: Job,
    start: CoroutineStart = coroutineStartForCurrentDispatchBehavior,
    block: suspend CoroutineScope.() -> Unit,
): Job =
    launch(start = start) {
        if (isDetectTapGesturesImmediateCoroutineDispatchEnabled) {
            resetJob.join()
        }
        block()
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

private class PressGestureScopeImpl(density: Density) : PressGestureScope, Density by density {
    private var isReleased = false
    private var isCanceled = false
    private val mutex = Mutex(locked = false)

    /** Called when a gesture has been canceled. */
    fun cancel() {
        isCanceled = true
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }

    /** Called when all pointers are up. */
    fun release() {
        isReleased = true
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }

    /** Called when a new gesture has started. */
    suspend fun reset() {
        mutex.lock()
        isReleased = false
        isCanceled = false
    }

    override suspend fun awaitRelease() {
        if (!tryAwaitRelease()) {
            throw GestureCancellationException("The press gesture was canceled.")
        }
    }

    override suspend fun tryAwaitRelease(): Boolean {
        if (!isReleased && !isCanceled) {
            mutex.lock()
            mutex.unlock()
        }
        return isReleased
    }
}