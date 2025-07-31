package com.eblan.launcher.domain.framework

import kotlinx.coroutines.flow.Flow

interface WallpaperManagerWrapper {
    val hintSupportsDarkText: Int

    val hintSupportsDarkTheme: Int

    fun getColorsChanged(): Flow<Int?>
}