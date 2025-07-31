package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo

interface AndroidAppWidgetHostWrapper {
    fun startListening()

    fun stopListening()

    fun allocateAppWidgetId(): Int

    fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): AppWidgetHostView
}