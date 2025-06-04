package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.launcherapps.LauncherAppsController

val LocalLauncherApps = staticCompositionLocalOf<LauncherAppsController> {
    error("No LauncherApps provided")
}