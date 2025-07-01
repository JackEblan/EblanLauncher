package com.eblan.launcher.feature.home.util

import androidx.compose.foundation.gestures.PressGestureScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

suspend fun PressGestureScope.pressGridItem(
    longPressTimeoutMillis: Long,
    onTap: (() -> Unit)? = null,
    onLongPress: (suspend () -> Unit)? = null,
    onDragging: (suspend () -> Unit)? = null,
) {
    try {
        withTimeout(longPressTimeoutMillis) {
            val released = tryAwaitRelease()

            if (released) {
                onTap?.invoke()
            }
        }
    } catch (e: TimeoutCancellationException) {
        onLongPress?.invoke()

        val released = tryAwaitRelease()

        if (!released) {
            onDragging?.invoke()
        }
    }
}
