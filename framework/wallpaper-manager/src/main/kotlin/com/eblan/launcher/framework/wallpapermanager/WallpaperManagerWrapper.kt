package com.eblan.launcher.framework.wallpapermanager

interface WallpaperManagerWrapper {
    suspend fun getWallpaper(): ByteArray?
}