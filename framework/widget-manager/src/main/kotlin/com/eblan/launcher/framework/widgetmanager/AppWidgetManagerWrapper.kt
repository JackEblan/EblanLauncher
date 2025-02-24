package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetProviderInfo

interface AppWidgetManagerWrapper {
    fun getInstalledProviders(): List<AppWidgetProviderInfo>

    suspend fun getInstalledProviderPackageNames(): List<String>

    suspend fun getInstalledProviderByPackageName(packageName: String): List<AppWidgetProviderInfo>
}