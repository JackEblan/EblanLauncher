package com.eblan.launcher.framework.launcherapps

import android.appwidget.AppWidgetProviderInfo
import android.content.pm.LauncherApps
import android.os.Build
import androidx.annotation.RequiresApi

interface PinItemRequestWrapper {
    fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?)

    fun getPinItemRequest(): LauncherApps.PinItemRequest?

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAppWidgetProviderInfo(): AppWidgetProviderInfo?

    @RequiresApi(Build.VERSION_CODES.O)
    fun accept(appWidgetId: Int): Boolean?
}