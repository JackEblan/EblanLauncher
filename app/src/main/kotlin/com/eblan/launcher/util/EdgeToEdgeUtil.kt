package com.eblan.launcher.util

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.eblan.launcher.domain.model.DarkThemeConfig

fun ComponentActivity.handleEdgeToEdge(darkThemeConfig: DarkThemeConfig) {
    when (darkThemeConfig) {
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
