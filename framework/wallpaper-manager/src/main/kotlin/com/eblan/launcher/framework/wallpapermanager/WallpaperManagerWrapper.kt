package com.eblan.launcher.framework.wallpapermanager

interface WallpaperManagerWrapper {
    fun setWallpaperOffsetSteps(xStep: Float, yStep: Float)

    fun setWallpaperOffsets(
        windowToken: android.os.IBinder,
        xOffset: Float,
        yOffset: Float,
    )
}