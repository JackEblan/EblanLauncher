package com.eblan.launcher.framework.launcherapps

import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi

interface AndroidLauncherAppsWrapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPinItemRequest(intent: Intent): LauncherApps.PinItemRequest

    fun startMainActivity(componentName: String?)

    fun startShortcut(packageName: String, id: String)

    fun getShortcutIconDrawable(shortcutInfo: ShortcutInfo, density: Int): Drawable
}