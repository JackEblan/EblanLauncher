package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SizeF
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
        minWidth: Int,
        minHeight: Int,
    ): AppWidgetHostView {
        return appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                updateAppWidgetSize(
                    Bundle.EMPTY,
                    listOf(
                        SizeF(minWidth.toFloat(), minHeight.toFloat()),
                    ),
                )
            } else {
                updateAppWidgetSize(
                    Bundle.EMPTY,
                    minWidth,
                    minHeight,
                    minWidth,
                    minHeight,
                )
            }
        }
    }

    override fun deleteAppWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }
}