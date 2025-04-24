package com.eblan.launcher.framework.windowmanager

import com.eblan.launcher.domain.model.ScreenSize

interface WindowManagerWrapper {
    fun getSize(): ScreenSize
}