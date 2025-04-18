package com.eblan.launcher.designsystem.local

import android.appwidget.AppWidgetManager
import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper

/**
 * Provides a CompositionLocal for an [AppWidgetManagerWrapper] instance.
 *
 * This CompositionLocal is used to store and provide the [AppWidgetManager],
 *
 * [AppWidgetManager] is tightly coupled to the UI. By exposing the [AppWidgetManager]
 * as a CompositionLocal, any composable within the hierarchy can easily access it,
 * without needing to pass it down explicitly through function parameters.
 */
val LocalAppWidgetManager = staticCompositionLocalOf<AppWidgetManagerWrapper> {
    error("No AppWidgetManager provided")
}