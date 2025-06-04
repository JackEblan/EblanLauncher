package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerController

val LocalAppWidgetManager = staticCompositionLocalOf<AppWidgetManagerController> {
    error("No AppWidgetManager provided")
}