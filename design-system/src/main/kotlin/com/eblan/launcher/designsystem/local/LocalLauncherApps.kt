package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper

val LocalLauncherApps = staticCompositionLocalOf<LauncherAppsWrapper> {
    error("No LauncherAppsWrapper provided")
}