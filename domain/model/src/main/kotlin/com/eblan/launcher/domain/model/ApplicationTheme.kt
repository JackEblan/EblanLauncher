package com.eblan.launcher.domain.model

data class ApplicationTheme(
    val themeBrand: ThemeBrand,
    val darkThemeConfig: DarkThemeConfig,
    val systemBarThemeConfig: SystemBarThemeConfig,
    val dynamicTheme: Boolean,
)
