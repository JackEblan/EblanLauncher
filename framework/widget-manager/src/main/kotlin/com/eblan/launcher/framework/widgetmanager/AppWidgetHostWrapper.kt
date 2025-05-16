package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import com.android.launcher3.OnTouchEventListener
import com.android.launcher3.widget.LauncherAppWidgetHost

interface AppWidgetHostWrapper {
    val appWidgetHost: LauncherAppWidgetHost

    fun startListening()

    fun stopListening()

    fun allocateAppWidgetId(): Int

    fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): AppWidgetHostView

    fun deleteAppWidgetId(appWidgetId: Int)

    fun setOnTouchEventListener(onTouchEventListener: OnTouchEventListener)
}