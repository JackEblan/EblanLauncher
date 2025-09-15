package com.eblan.launcher.ui.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

val LocalAppWidgetHost = staticCompositionLocalOf<AndroidAppWidgetHostWrapper> {
    error("No AppWidgetHost provided")
}

val LocalAppWidgetManager = staticCompositionLocalOf<AndroidAppWidgetManagerWrapper> {
    error("No AppWidgetManager provided")
}

val LocalLauncherApps = staticCompositionLocalOf<AndroidLauncherAppsWrapper> {
    error("No LauncherAppsWrapper provided")
}

val LocalPinItemRequest = staticCompositionLocalOf<PinItemRequestWrapper> {
    error("No PinItemRequest provided")
}

val LocalWallpaperManager = staticCompositionLocalOf<AndroidWallpaperManagerWrapper> {
    error("No WallpaperManagerWrapper provided")
}

val LocalPackageManager = staticCompositionLocalOf<AndroidPackageManagerWrapper> {
    error("No AndroidPackageManager provided")
}
