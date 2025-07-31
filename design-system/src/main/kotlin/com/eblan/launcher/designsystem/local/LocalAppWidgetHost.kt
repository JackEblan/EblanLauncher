package com.eblan.launcher.designsystem.local

import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper

val LocalAppWidgetHost = staticCompositionLocalOf<AndroidAppWidgetHostWrapper> {
    error("No AppWidgetHost provided")
}
