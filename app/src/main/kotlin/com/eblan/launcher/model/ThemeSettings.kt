package com.eblan.launcher.model

import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.ThemeBrand

data class ThemeSettings(
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val dynamicTheme: Boolean,
)
