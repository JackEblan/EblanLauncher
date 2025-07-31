package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper

val LocalLauncherApps = staticCompositionLocalOf<AndroidLauncherAppsWrapper> {
    error("No LauncherAppsWrapper provided")
}