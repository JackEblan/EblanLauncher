package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.DarkThemeConfig

interface ResourcesWrapper {
    fun getSystemTheme(): DarkThemeConfig
}