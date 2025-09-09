package com.eblan.launcher.ui.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

val LocalAppWidgetManager = staticCompositionLocalOf<AndroidAppWidgetManagerWrapper> {
    error("No AppWidgetManager provided")
}