package com.eblan.launcher.feature.home.util

import androidx.compose.ui.graphics.Color
import com.eblan.launcher.domain.model.TextColor

fun getGridItemTextColor(
    systemTextColor: TextColor,
    gridItemTextColor: TextColor,
): Color {
    return when (gridItemTextColor) {
        TextColor.System -> {
            getSystemTextColor(textColor = systemTextColor)
        }

        TextColor.Light -> {
            Color.White
        }

        TextColor.Dark -> {
            Color.Black
        }
    }
}

fun getSystemTextColor(textColor: TextColor): Color {
    return when (textColor) {
        TextColor.System -> {
            Color.Unspecified
        }

        TextColor.Light -> {
            Color.White
        }

        TextColor.Dark -> {
            Color.Black
        }
    }
}