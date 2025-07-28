package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.wallpapermanager.WallpaperManagerWrapper

val LocalWallpaperManager = staticCompositionLocalOf<WallpaperManagerWrapper> {
    error("No WallpaperManagerWrapper provided")
}