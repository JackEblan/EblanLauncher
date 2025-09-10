/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.framework.widgetmanager

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultAppWidgetManagerWrapper @Inject constructor(@ApplicationContext private val context: Context) :
    AppWidgetManagerWrapper, AndroidAppWidgetManagerWrapper {
    private val appWidgetManager = AppWidgetManager.getInstance(context)

    private val packageManager = context.packageManager

    override val hasSystemFeatureAppWidgets =
        packageManager.hasSystemFeature(PackageManager.FEATURE_APP_WIDGETS)

    override suspend fun getInstalledProviders(): List<AppWidgetManagerAppWidgetProviderInfo> {
        return appWidgetManager.installedProviders.map { appWidgetProviderInfo ->
            appWidgetProviderInfo.toEblanAppWidgetProviderInfo()
        }
    }

    override fun getAppWidgetInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        return appWidgetManager.getAppWidgetInfo(appWidgetId)
    }

    override fun bindAppWidgetIdIfAllowed(appWidgetId: Int, provider: ComponentName?): Boolean {
        return appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, provider)
    }

    override fun updateAppWidgetOptions(appWidgetId: Int, options: Bundle) {
        appWidgetManager.updateAppWidgetOptions(appWidgetId, options)
    }

    private suspend fun AppWidgetProviderInfo.toEblanAppWidgetProviderInfo(): AppWidgetManagerAppWidgetProviderInfo {
        val preview = loadPreviewImage(context, 0)?.toByteArray()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AppWidgetManagerAppWidgetProviderInfo(
                className = provider.className,
                packageName = provider.packageName,
                componentName = provider.flattenToString(),
                configure = configure?.flattenToString(),
                targetCellWidth = targetCellWidth,
                targetCellHeight = targetCellHeight,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                preview = preview,
            )
        } else {
            AppWidgetManagerAppWidgetProviderInfo(
                className = provider.className,
                packageName = provider.packageName,
                componentName = provider.flattenToString(),
                configure = configure?.flattenToString(),
                targetCellWidth = 0,
                targetCellHeight = 0,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = 0,
                maxResizeHeight = 0,
                preview = preview,
            )
        }
    }
}
