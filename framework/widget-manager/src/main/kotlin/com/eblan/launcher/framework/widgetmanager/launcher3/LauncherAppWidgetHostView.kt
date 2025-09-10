package com.eblan.launcher.framework.widgetmanager.launcher3

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.AdapterView
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

internal class LauncherAppWidgetHostView(context: Context) : AppWidgetHostView(context),
    View.OnLongClickListener {

    private val mLongPressHelper = CheckLongPressHelper(view = this, listener = this)

    private var mIsScrollable = false
    private val downPoint = PointF()
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downPoint.set(ev.x, ev.y)

                if (mIsScrollable) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = ev.x - downPoint.x
                val dy = ev.y - downPoint.y

                val distance = sqrt(dx * dx + dy * dy)

                val angle = abs(Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())))

                if (distance > slop) {
                    if (mIsScrollable) {
                        if (angle < 45 || angle > 135) {
                            // Horizontal swipe → let pager do the swipe
                            parent?.requestDisallowInterceptTouchEvent(false)
                        } else {
                            // Vertical swipe → let this widget scroll
                            parent?.requestDisallowInterceptTouchEvent(true)
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        mLongPressHelper.onTouchEvent(ev)

        return mLongPressHelper.hasPerformedLongPress()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mLongPressHelper.onTouchEvent(ev = ev)

        return true
    }

    override fun onLongClick(view: View): Boolean {
        if (mIsScrollable) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }

        // Trigger CheckLongPress ACTION_CANCEL thus cancelling the long press callback
        cancelMotionEvent(view = view)

        return view.performLongClick()
    }

    override fun cancelLongPress() {
        super.cancelLongPress()

        mLongPressHelper.cancelLongPress()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mIsScrollable = checkScrollableRecursively(this)
    }

    private fun checkScrollableRecursively(viewGroup: ViewGroup): Boolean {
        if (viewGroup is AdapterView<*>) {
            return true
        } else {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is ViewGroup) {
                    if (checkScrollableRecursively(child)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun cancelMotionEvent(view: View) {
        val cancel = MotionEvent.obtain(
            0,
            0,
            MotionEvent.ACTION_CANCEL,
            0f,
            0f,
            0
        )

        view.dispatchTouchEvent(cancel)

        cancel.recycle()
    }

}
