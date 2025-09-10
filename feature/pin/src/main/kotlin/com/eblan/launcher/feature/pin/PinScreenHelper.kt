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
package com.eblan.launcher.feature.pin

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

fun handleGridItem(
    gridItem: GridItem?,
    appWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onAddedToHomeScreenToast: (String) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    if (gridItem == null) return

    val data = gridItem.data

    if (data is GridItemData.Widget) {
        val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

        onUpdateAppWidgetId(appWidgetId)

        onAddPinWidget(
            gridItem = gridItem,
            appWidgetId = appWidgetId,
            appWidgetManager = appWidgetManager,
            data = data,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onLaunch = onLaunch,
        )

        onAddedToHomeScreenToast(
            """
                ${gridItem.page}
                ${gridItem.startRow}
                ${gridItem.startColumn}
            """.trimIndent(),
        )
    }
}

fun onAddPinWidget(
    gridItem: GridItem,
    appWidgetId: Int,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    data: GridItemData.Widget,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(data.componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateWidgetGridItem(gridItem.copy(data = newData))
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
}

fun handleAppWidgetLauncherResult(
    gridItem: GridItem?,
    result: ActivityResult,
    onUpdateWidgetGridItem: (GridItem) -> Unit,
    onDeleteAppWidgetId: () -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId =
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateWidgetGridItem(gridItem.copy(data = newData))
    } else {
        onDeleteAppWidgetId()
    }
}

fun handleIsBoundWidget(
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest?,
    isBoundWidget: Boolean,
    appWidgetId: Int,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateGridItems: () -> Unit,
) {
    if (gridItem == null || !isBoundWidget || pinItemRequest == null) return

    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest.isValid &&
        pinItemRequest.accept(extras)
    ) {
        onUpdateGridItems()
    } else {
        onDeleteGridItem(gridItem)
    }
}

fun handleDeleteAppWidgetId(
    gridItem: GridItem?,
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && deleteAppWidgetId) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onDeleteGridItem(gridItem.copy(data = newData))
    }
}
