package com.eblan.launcher.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.model.MainActivityThemeSettings
import com.eblan.launcher.model.ThemeSettings

fun ComponentActivity.handleWallpaperEdgeToEdge(mainActivityThemeSettings: MainActivityThemeSettings) {
    when (mainActivityThemeSettings.themeSettings.darkThemeConfig) {
        DarkThemeConfig.System -> {
            if (mainActivityThemeSettings.hintSupportsDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.light(
                        scrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ),
                    navigationBarStyle = SystemBarStyle.light(
                        scrim = Color.TRANSPARENT,
                        darkScrim = Color.TRANSPARENT,
                    ),
                )
            } else {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                )
            }
        }

        DarkThemeConfig.Light -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
            )
        }

        DarkThemeConfig.Dark -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
            )
        }
    }
}

fun ComponentActivity.handleEdgeToEdge(themeSettings: ThemeSettings) {
    when (themeSettings.darkThemeConfig) {
        DarkThemeConfig.System -> {
            enableEdgeToEdge()
        }

        DarkThemeConfig.Light -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
                navigationBarStyle = SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
            )
        }

        DarkThemeConfig.Dark -> {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.dark(scrim = Color.TRANSPARENT),
            )
        }
    }
}
