package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.framework.AppWidgetManagerDomainWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidAppWidgetManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetManagerDomainWrapper, AppWidgetManagerWrapper {
    private val appWidgetManager = AppWidgetManager.getInstance(context)

    private val packageManager = context.packageManager

    override suspend fun getInstalledProviders(): List<AppWidgetManagerAppWidgetProviderInfo> {
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            withContext(Dispatchers.Default) {
                appWidgetManager.installedProviders.map { appWidgetProviderInfo ->
                    val preview = appWidgetProviderInfo.loadPreviewImage(context, 0)?.toByteArray()

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        AppWidgetManagerAppWidgetProviderInfo(
                            className = appWidgetProviderInfo.provider.className,
                            packageName = appWidgetProviderInfo.provider.packageName,
                            componentName = appWidgetProviderInfo.provider.flattenToString(),
                            targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                            targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                            minWidth = appWidgetProviderInfo.minWidth,
                            minHeight = appWidgetProviderInfo.minHeight,
                            resizeMode = appWidgetProviderInfo.resizeMode,
                            minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                            minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                            maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                            maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                            preview = preview,
                        )
                    } else {
                        AppWidgetManagerAppWidgetProviderInfo(
                            className = appWidgetProviderInfo.provider.className,
                            packageName = appWidgetProviderInfo.provider.packageName,
                            componentName = appWidgetProviderInfo.provider.flattenToString(),
                            targetCellWidth = 0,
                            targetCellHeight = 0,
                            minWidth = appWidgetProviderInfo.minWidth,
                            minHeight = appWidgetProviderInfo.minHeight,
                            resizeMode = appWidgetProviderInfo.resizeMode,
                            minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                            minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                            maxResizeWidth = 0,
                            maxResizeHeight = 0,
                            preview = preview,
                        )
                    }
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