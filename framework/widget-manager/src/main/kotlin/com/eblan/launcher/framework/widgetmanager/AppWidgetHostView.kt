package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHostView
import android.view.View
import android.view.ViewGroup

fun AppWidgetHostView.clearPressed(view: View): Boolean {
    if (view.isPressed) {
        view.isPressed = false
        return true
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            if (clearPressed(view.getChildAt(i))) {
                return true
            }
        }
    }

    return false
}