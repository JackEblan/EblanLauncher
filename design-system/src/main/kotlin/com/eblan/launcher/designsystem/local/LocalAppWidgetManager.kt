package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper

val LocalAppWidgetManager = staticCompositionLocalOf<AppWidgetManagerWrapper> {
    error("No AppWidgetManager provided")
}