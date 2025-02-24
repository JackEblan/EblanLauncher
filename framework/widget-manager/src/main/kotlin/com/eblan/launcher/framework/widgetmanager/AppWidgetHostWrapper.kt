package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetProviderInfo
import android.view.View

interface AppWidgetHostWrapper {
    fun startListening()
    fun stopListening()
    fun allocateAppWidgetId(): Int
    fun createView(appWidgetId: Int, appWidgetProviderInfo: AppWidgetProviderInfo): View
}