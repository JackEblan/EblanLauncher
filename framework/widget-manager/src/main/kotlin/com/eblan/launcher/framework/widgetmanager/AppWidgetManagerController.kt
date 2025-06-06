package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.os.Bundle

interface AppWidgetManagerController {
    fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo?

    fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName?): Boolean

    fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle)
}