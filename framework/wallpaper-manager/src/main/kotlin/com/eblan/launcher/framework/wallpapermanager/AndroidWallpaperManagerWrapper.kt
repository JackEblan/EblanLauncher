package com.eblan.launcher.framework.wallpapermanager

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class AndroidWallpaperManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    WallpaperManagerWrapper {
    private val wallpaperManager = WallpaperManager.getInstance(context)

    override val supportsColorHints = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    @RequiresApi(Build.VERSION_CODES.S)
    override val hintSupportsDarkText = WallpaperColors.HINT_SUPPORTS_DARK_TEXT

    @RequiresApi(Build.VERSION_CODES.S)
    override val hintSupportsDarkTheme = WallpaperColors.HINT_SUPPORTS_DARK_THEME

    @RequiresApi(Build.VERSION_CODES.S)
    override fun getColorsChanged(): Flow<Int?> {
        return callbackFlow {
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
        }
    }
}