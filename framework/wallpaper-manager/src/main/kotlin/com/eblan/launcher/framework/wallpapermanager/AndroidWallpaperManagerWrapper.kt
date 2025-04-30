package com.eblan.launcher.framework.wallpapermanager

import android.app.WallpaperManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class AndroidWallpaperManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    WallpaperManagerWrapper {
    private val wallpaperManager = WallpaperManager.getInstance(context)

}