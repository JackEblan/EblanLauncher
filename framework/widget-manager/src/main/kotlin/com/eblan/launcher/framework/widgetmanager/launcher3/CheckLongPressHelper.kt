package com.eblan.launcher.framework.widgetmanager.launcher3

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

internal class CheckLongPressHelper(
    private val view: View,
    private val listener: View.OnLongClickListener,
) {
    private val slop = ViewConfiguration.get(view.context).scaledTouchSlop.toFloat()

    private var hasPerformedLongPress = false

    private var pendingCheckForLongPress: Runnable? = null

    private val downPoint = PointF()

    fun onTouchEvent(ev: MotionEvent) {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(ev.x, ev.y)

                cancelLongPress()

                postCheckForLongPress()
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                cancelLongPress()
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downPoint.x
                val dy = ev.y - downPoint.y

                if (dx * dx + dy * dy > slop * slop) {
                    cancelLongPress()
                }
            }
        }
    }

    fun cancelLongPress() {
        hasPerformedLongPress = false

        clearCallbacks()
    }

    fun hasPerformedLongPress(): Boolean = hasPerformedLongPress

    private fun postCheckForLongPress() {
        hasPerformedLongPress = false

        if (pendingCheckForLongPress == null) {
            pendingCheckForLongPress = Runnable { triggerLongPress() }
        }

        view.postDelayed(
            pendingCheckForLongPress,
            ViewConfiguration.getLongPressTimeout().toLong()
        )
    }

    private fun triggerLongPress() {
        if (view.parent != null &&
            view.hasWindowFocus() &&
            !view.isPressed &&
            !hasPerformedLongPress
        ) {
            if (listener.onLongClick(view)) {
                view.isPressed = false
                hasPerformedLongPress = true
            }

            clearCallbacks()
        }
    }

    private fun clearCallbacks() {
        if (pendingCheckForLongPress != null) {
            view.removeCallbacks(pendingCheckForLongPress)

            pendingCheckForLongPress = null
        }
    }
}
