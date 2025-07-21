package com.eblan.launcher.framework.launcherapps

import android.content.Context
import android.content.pm.LauncherApps
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidPinItemRequestWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    PinItemRequestWrapper {
    private var pinItemRequest: LauncherApps.PinItemRequest? = null

    override fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?) {
        this.pinItemRequest = pinItemRequest
    }

    override fun getPinItemRequest(): LauncherApps.PinItemRequest? {
        return pinItemRequest
    }
}