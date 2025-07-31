package com.eblan.launcher.framework.wallpapermanager

interface AndroidWallpaperManagerWrapper {
    fun setWallpaperOffsetSteps(xStep: Float, yStep: Float)

    fun setWallpaperOffsets(
        windowToken: android.os.IBinder,
        xOffset: Float,
        yOffset: Float,
    )
}