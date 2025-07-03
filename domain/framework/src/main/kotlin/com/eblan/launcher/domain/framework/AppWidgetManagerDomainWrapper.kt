package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo

interface AppWidgetManagerDomainWrapper {
    val hasSystemFeatureAppWidgets: Boolean

    suspend fun getInstalledProviders(): List<AppWidgetManagerAppWidgetProviderInfo>
}