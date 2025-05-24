package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidAppWidgetManagerWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppWidgetManagerWrapper {
    private val appWidgetManager = AppWidgetManager.getInstance(context)

    private val packageManager = context.packageManager

    override suspend fun getInstalledProviders(): List<EblanAppWidgetProviderInfo> {
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            appWidgetManager.installedProviders.map { appWidgetProviderInfo ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    EblanAppWidgetProviderInfo(
                        packageName = appWidgetProviderInfo.provider.packageName,
                        componentName = appWidgetProviderInfo.provider.flattenToString(),
                        minWidth = appWidgetProviderInfo.minWidth,
                        minHeight = appWidgetProviderInfo.minHeight,
                        resizeMode = appWidgetProviderInfo.resizeMode,
                        minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                        minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                        maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                        maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                        targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                        targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                    )
                } else {
                    EblanAppWidgetProviderInfo(
                        packageName = appWidgetProviderInfo.provider.packageName,
                        componentName = appWidgetProviderInfo.provider.flattenToString(),
                        minWidth = appWidgetProviderInfo.minWidth,
                        minHeight = appWidgetProviderInfo.minHeight,
                        resizeMode = appWidgetProviderInfo.resizeMode,
                        minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                        minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                        maxResizeWidth = 0,
                        maxResizeHeight = 0,
                        targetCellWidth = 0,
                        targetCellHeight = 0,
                    )
                }
            }
        } else {
            emptyList()
        }
    }

    override fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        return appWidgetManager.getAppWidgetInfo(appWidgetId)
    }

    override fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName?): Boolean {
        return appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)
    }

    override fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle) {
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
    }
}