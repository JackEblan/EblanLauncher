package com.eblan.launcher.framework.iconpackmanager

import android.graphics.drawable.Drawable

interface AndroidIconPackManager {
    suspend fun loadDrawableFromIconPack(
        packageName: String,
        drawableName: String,
    ): Drawable?
}