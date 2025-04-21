package com.eblan.launcher.framework.wallpapermanager

import android.app.WallpaperManager
import android.content.Context
import com.eblan.launcher.common.util.toByteArray
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class AndroidWallpaperManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    WallpaperManagerWrapper {
    private val wallpaperManager = WallpaperManager.getInstance(context)

    override suspend fun getWallpaper(): ByteArray? {
        return withContext(Dispatchers.IO) {
            wallpaperManager.drawable?.toByteArray()
        }
    }
}