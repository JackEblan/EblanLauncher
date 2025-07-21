package com.eblan.launcher.framework.launcherapps

import android.content.pm.LauncherApps

interface PinItemRequestWrapper {
    fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?)

    fun getPinItemRequest(): LauncherApps.PinItemRequest?
}