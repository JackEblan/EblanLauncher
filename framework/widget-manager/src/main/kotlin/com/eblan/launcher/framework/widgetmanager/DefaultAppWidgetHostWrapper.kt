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

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.launcher3.LauncherAppWidgetHost
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultAppWidgetHostWrapper @Inject constructor(@param:ApplicationContext private val context: Context) :
    AppWidgetHostWrapper, AndroidAppWidgetHostWrapper {
    private val appWidgetHost = LauncherAppWidgetHost(context, 2814)

    override fun startListening() {
        appWidgetHost.startListening()
    }

    override fun stopListening() {
        appWidgetHost.stopListening()
    }

    override fun allocateAppWidgetId(): Int {
        return appWidgetHost.allocateAppWidgetId()
    }

    @Suppress("DEPRECATION")
    override fun createView(
        appWidgetId: Int,
        appWidgetProviderInfo: AppWidgetProviderInfo,
        minWidth: Int,
        minHeight: Int,
    ): AppWidgetHostView {
        return appWidgetHost.createView(context, appWidgetId, appWidgetProviderInfo).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                updateAppWidgetSize(
                    Bundle.EMPTY,
                    listOf(
                        SizeF(minWidth.toFloat(), minHeight.toFloat()),
                    ),
                )
            } else {
                updateAppWidgetSize(
                    Bundle.EMPTY,
                    minWidth,
                    minHeight,
                    minWidth,
                    minHeight,
                )
            }
        }
    }

    override fun deleteAppWidgetId(appWidgetId: Int) {
        appWidgetHost.deleteAppWidgetId(appWidgetId)
    }
}
