package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper

val LocalWallpaperManager = staticCompositionLocalOf<AndroidWallpaperManagerWrapper> {
    error("No WallpaperManagerWrapper provided")
}