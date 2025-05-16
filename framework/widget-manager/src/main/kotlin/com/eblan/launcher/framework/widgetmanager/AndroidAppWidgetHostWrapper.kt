package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.android.launcher3.OnTouchEventListener
import com.android.launcher3.widget.LauncherAppWidgetHost
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidAppWidgetHostWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetHostWrapper {
    override val appWidgetHost = LauncherAppWidgetHost(context, 2814)

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

    override fun setOnTouchEventListener(onTouchEventListener: OnTouchEventListener) {
        appWidgetHost.setOnTouchEventListener(onTouchEventListener)
    }
}