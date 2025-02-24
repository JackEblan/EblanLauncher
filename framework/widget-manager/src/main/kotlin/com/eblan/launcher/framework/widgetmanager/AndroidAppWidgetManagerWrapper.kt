package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidAppWidgetManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetManagerWrapper {
    private val appWidgetManager = AppWidgetManager.getInstance(context)

    private val packageManager = context.packageManager

    override fun getInstalledProviders(): List<AppWidgetProviderInfo> {
        return if (packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
            appWidgetManager.installedProviders
        } else {
            emptyList()
        }
    }

    override suspend fun getInstalledProviderPackageNames(): List<String> {
        return withContext(Dispatchers.Default) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
                appWidgetManager.installedProviders.map { it.provider.packageName }.distinct()
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getInstalledProviderByPackageName(packageName: String): List<AppWidgetProviderInfo> {
        return withContext(Dispatchers.Default) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)) {
                appWidgetManager.installedProviders.filter { it.provider.packageName == packageName }
            } else {
                emptyList()
            }
        }
    }

    override fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo {
        return appWidgetManager.getAppWidgetInfo(appWidgetId)
    }

    override fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName): Boolean {
        return appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)
    }
}