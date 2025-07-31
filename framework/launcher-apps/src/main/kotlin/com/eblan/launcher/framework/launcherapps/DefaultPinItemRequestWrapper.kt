package com.eblan.launcher.framework.launcherapps

import android.content.pm.LauncherApps
import javax.inject.Inject

internal class DefaultPinItemRequestWrapper @Inject constructor() :
    PinItemRequestWrapper {
    private var pinItemRequest: LauncherApps.PinItemRequest? = null

    override fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?) {
        this.pinItemRequest = pinItemRequest
    }

    override fun getPinItemRequest(): LauncherApps.PinItemRequest? {
        return pinItemRequest
    }
}