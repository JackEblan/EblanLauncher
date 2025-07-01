package com.eblan.launcher.feature.home.util

import androidx.compose.foundation.gestures.PressGestureScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend fun PressGestureScope.pressGridItem(
    longPressTimeoutMillis: Long,
    onTap: () -> Unit,
    onLongPress: suspend () -> Unit,
    onDragging: () -> Unit,
) {
    try {
        withTimeout(longPressTimeoutMillis) {
            val released = tryAwaitRelease()

            if (released) {
                onTap()
            }
        }
    } catch (e: TimeoutCancellationException) {
        onLongPress()

        val released = tryAwaitRelease()

        if (!released) {
            onDragging()
        }
    }
}
