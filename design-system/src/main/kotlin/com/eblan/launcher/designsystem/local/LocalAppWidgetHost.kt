package com.eblan.launcher.designsystem.local

import android.appwidget.AppWidgetHost
import androidx.compose.runtime.staticCompositionLocalOf
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper

/**
 * Provides a CompositionLocal for an [AppWidgetHostWrapper] instance.
 *
 * This CompositionLocal is used to store and provide the AppWidgetHost,
 *
 * [AppWidgetHost] is tightly coupled to the UI. By exposing the [AppWidgetHost]
 * as a CompositionLocal, any composable within the hierarchy can easily access it,
 * without needing to pass it down explicitly through function parameters.
 */
val LocalAppWidgetHost = staticCompositionLocalOf<AppWidgetHostWrapper> {
    error("No AppWidgetHost provided")
}
