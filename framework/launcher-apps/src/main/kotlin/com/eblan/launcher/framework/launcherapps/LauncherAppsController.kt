package com.eblan.launcher.framework.launcherapps

import android.content.ComponentName
import android.graphics.Rect
import android.os.Bundle

interface LauncherAppsController {
    fun startMainActivity(
        component: ComponentName?,
        sourceBounds: Rect,
        opts: Bundle?,
    )
}