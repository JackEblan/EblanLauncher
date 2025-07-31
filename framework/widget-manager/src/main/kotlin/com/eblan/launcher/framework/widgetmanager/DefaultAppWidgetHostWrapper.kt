package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultAppWidgetHostWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetHostWrapper, AndroidAppWidgetHostWrapper {
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

    override fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): AppWidgetHostView {
        return appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo)
    }

    override fun deleteAppWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }
}