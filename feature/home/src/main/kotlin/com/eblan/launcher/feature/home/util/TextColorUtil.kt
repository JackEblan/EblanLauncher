package com.eblan.launcher.feature.home.util

import androidx.compose.ui.graphics.Color
import com.eblan.launcher.domain.model.TextColor

fun getGridItemTextColor(textColor: TextColor): Color {
    return when (textColor) {
        TextColor.System -> {
            getGridItemTextColor(textColor = textColor)
        }

        TextColor.Light -> {
            Color.White
        }

        TextColor.Dark -> {
            Color.Black
        }
    }
}