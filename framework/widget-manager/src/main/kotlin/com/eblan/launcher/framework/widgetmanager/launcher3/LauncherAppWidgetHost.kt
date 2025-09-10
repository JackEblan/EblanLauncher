package com.eblan.launcher.framework.widgetmanager.launcher3

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

internal class LauncherAppWidgetHost(
    context: Context,
    hostId: Int
) : AppWidgetHost(context, hostId) {
    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo
    ): AppWidgetHostView {
        return LauncherAppWidgetHostView(context)
    }
}