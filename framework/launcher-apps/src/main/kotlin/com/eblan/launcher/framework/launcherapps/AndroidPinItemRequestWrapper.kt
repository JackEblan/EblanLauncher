package com.eblan.launcher.framework.launcherapps

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidPinItemRequestWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    PinItemRequestWrapper {
    private var pinItemRequest: LauncherApps.PinItemRequest? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?) {
        this.pinItemRequest = pinItemRequest
    }

    override fun getPinItemRequest(): LauncherApps.PinItemRequest? {
        return pinItemRequest
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAppWidgetProviderInfo(): AppWidgetProviderInfo? {
        return pinItemRequest?.getAppWidgetProviderInfo(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun accept(appWidgetId: Int): Boolean? {
        val extras = Bundle().apply {
            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        return pinItemRequest?.accept(extras)
    }
}