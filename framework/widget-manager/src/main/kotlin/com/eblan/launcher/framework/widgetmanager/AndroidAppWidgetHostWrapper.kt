package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidAppWidgetHostWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetHostWrapper {
    private val appWidgetHost = AppWidgetHost(context, 2814)

    override fun startListening() {
        appWidgetHost.startListening()
    }

    override fun stopListening() {
        appWidgetHost.stopListening()
    }

    override fun allocateAppWidgetId(): Int {
        return appWidgetHost.allocateAppWidgetId()
    }

    override fun createView(appWidgetId: Int, appWidgetProviderInfo: AppWidgetProviderInfo): View {
        return appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo).apply {
            setAppWidget(appWidgetId, appWidgetProviderInfo)
        }
    }
}