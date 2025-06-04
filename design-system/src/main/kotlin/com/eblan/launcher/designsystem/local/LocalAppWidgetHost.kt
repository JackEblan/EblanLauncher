package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper

val LocalAppWidgetHost = staticCompositionLocalOf<AppWidgetHostWrapper> {
    error("No AppWidgetHost provided")
}
