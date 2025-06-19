package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import com.android.launcher3.OnTouchEventListener

interface AppWidgetHostWrapper {
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