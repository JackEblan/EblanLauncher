package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo

interface AppWidgetManagerDomainWrapper {
    suspend fun getInstalledProviders(): List<AppWidgetManagerAppWidgetProviderInfo>
}