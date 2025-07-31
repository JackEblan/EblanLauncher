package com.eblan.launcher.framework.wallpapermanager

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class DefaultWallpaperManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    WallpaperManagerWrapper, AndroidWallpaperManagerWrapper {
    private val wallpaperManager = WallpaperManager.getInstance(context)

    override val hintSupportsDarkText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        WallpaperColors.HINT_SUPPORTS_DARK_TEXT
    } else {
        0
    }

    override val hintSupportsDarkTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        WallpaperColors.HINT_SUPPORTS_DARK_THEME
    } else {
        0
    }

    override fun getColorsChanged(): Flow<Int?> {
        return callbackFlow {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                trySend(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.colorHints)

                val callback = WallpaperManager.OnColorsChangedListener { wallpaperColors, which ->
                    if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                        trySend(wallpaperColors?.colorHints)
                    }
                }

                wallpaperManager.addOnColorsChangedListener(
                    callback,
                    Handler(Looper.getMainLooper()),
                )

                awaitClose {
                    wallpaperManager.removeOnColorsChangedListener(callback)
                }
            } else {
                trySend(0)
            }
        }
    }

    override fun setWallpaperOffsetSteps(xStep: Float, yStep: Float) {
        wallpaperManager.setWallpaperOffsetSteps(xStep, yStep)
    }

    override fun setWallpaperOffsets(
        windowToken: android.os.IBinder,
        xOffset: Float,
        yOffset: Float,
    ) {
        wallpaperManager.setWallpaperOffsets(windowToken, xOffset, yOffset)
    }
}