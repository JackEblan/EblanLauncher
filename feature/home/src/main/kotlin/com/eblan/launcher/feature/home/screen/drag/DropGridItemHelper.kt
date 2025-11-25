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
package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

internal suspend fun handleDropGridItem(
    moveGridItemResult: MoveGridItemResult?,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    gridItemSource: GridItemSource,
    userManagerWrapper: AndroidUserManagerWrapper,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onLaunchShortcutConfigActivity: (Intent) -> Unit,
    onLaunchShortcutConfigActivityIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDragCancelAfterMove: () -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onToast: () -> Unit,
) {
    if (moveGridItemResult == null || !moveGridItemResult.isSuccess) {
        onDragCancelAfterMove()

        onToast()

        return
    }

    when (gridItemSource) {
        is GridItemSource.New -> {
            when (val data = gridItemSource.gridItem.data) {
                is GridItemData.Widget -> {
                    onDragEndWidget(
                        gridItem = gridItemSource.gridItem,
                        data = data,
                        androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                        appWidgetManager = appWidgetManager,
                        onLaunchWidgetIntent = onLaunchWidgetIntent,
                        onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                        onUpdateAppWidgetId = onUpdateAppWidgetId,
                    )
                }

                is GridItemData.ShortcutConfigActivity -> {
                    onDragEndShortcutConfigActivity(
                        gridItem = gridItemSource.gridItem,
                        data = data,
                        userManagerWrapper = userManagerWrapper,
                        launcherAppsWrapper = launcherAppsWrapper,
                        onLaunchShortcutConfigActivityIntent = onLaunchShortcutConfigActivity,
                        onLaunchShortcutConfigActivityIntentSenderRequest = onLaunchShortcutConfigActivityIntentSenderRequest,
                        onDeleteGridItemCache = onDeleteGridItemCache,
                    )
                }

                else -> {
                    onDragEndAfterMove(
                        moveGridItemResult.movingGridItem,
                        moveGridItemResult.conflictingGridItem,
                    )
                }
            }
        }

        is GridItemSource.Pin -> {
            when (val data = gridItemSource.gridItem.data) {
                is GridItemData.ShortcutInfo -> {
                    onDragEndPinShortcut(
                        moveGridItemResult = moveGridItemResult,
                        pinItemRequest = gridItemSource.pinItemRequest,
                        gridItem = gridItemSource.gridItem,
                        onDeleteGridItemCache = onDeleteGridItemCache,
                        onDragEndAfterMove = onDragEndAfterMove,
                    )
                }

                is GridItemData.Widget -> {
                    onDragEndWidget(
                        gridItem = gridItemSource.gridItem,
                        data = data,
                        androidAppWidgetHostWrapper = androidAppWidgetHostWrapper,
                        appWidgetManager = appWidgetManager,
                        onLaunchWidgetIntent = onLaunchWidgetIntent,
                        onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                        onUpdateAppWidgetId = onUpdateAppWidgetId,
                    )
                }

                else -> Unit
            }
        }

        else -> {
            onDragEndAfterMove(
                moveGridItemResult.movingGridItem,
                moveGridItemResult.conflictingGridItem,
            )
        }
    }
}

internal fun handleAppWidgetLauncherResult(
    result: ActivityResult,
    gridItem: GridItem,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteAppWidgetId: () -> Unit,
) {
    val data = (gridItem.data as? GridItemData.Widget)
        ?: error("Expected GridItemData.Widget")

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateGridItemDataCache(gridItem.copy(data = newData))
    } else {
        onDeleteAppWidgetId()
    }
}

internal fun handleConfigureLauncherResult(
    moveGridItemResult: MoveGridItemResult?,
    updatedGridItem: GridItem?,
    resultCode: Int,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
) {
    requireNotNull(moveGridItemResult)

    requireNotNull(updatedGridItem)

    val data = (updatedGridItem.data as? GridItemData.Widget)
        ?: error("Expected GridItemData as Widget")

    if (resultCode == Activity.RESULT_OK) {
        onDragEndAfterMove(
            updatedGridItem,
            moveGridItemResult.conflictingGridItem,
        )
    } else {
        onDeleteWidgetGridItemCache(updatedGridItem, data.appWidgetId)
    }
}

internal fun handleDeleteAppWidgetId(
    gridItem: GridItem,
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    onDeleteWidgetGridItemCache: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
) {
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
        deleteAppWidgetId
    ) {
        check(gridItem.data is GridItemData.Widget)

        onDeleteWidgetGridItemCache(gridItem, appWidgetId)
    }
}

internal fun handleBoundWidget(
    gridItemSource: GridItemSource,
    updatedGridItem: GridItem?,
    moveGridItemResult: MoveGridItemResult?,
    packageManager: AndroidPackageManagerWrapper,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val data = (updatedGridItem?.data as? GridItemData.Widget) ?: return

    requireNotNull(moveGridItemResult)

    when (gridItemSource) {
        is GridItemSource.New -> {
            configureComponent(
                appWidgetId = data.appWidgetId,
                configure = data.configure,
                updatedGridItem = updatedGridItem,
                conflictingGridItem = moveGridItemResult.conflictingGridItem,
                packageManager = packageManager,
                onConfigure = onConfigure,
                onDragEndAfterMove = onDragEndAfterMove,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                appWidgetId = data.appWidgetId,
                gridItem = updatedGridItem,
                pinItemRequest = gridItemSource.pinItemRequest,
                updatedGridItem = updatedGridItem,
                conflictingGridItem = moveGridItemResult.conflictingGridItem,
                onDragEndAfterMove = onDragEndAfterMove,
                onDeleteGridItemCache = onDeleteGridItemCache,
            )
        }

        else -> Unit
    }
}

private fun onDragEndWidget(
    gridItem: GridItem,
    data: GridItemData.Widget,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
) {
    val appWidgetId = androidAppWidgetHostWrapper.allocateAppWidgetId()

    onUpdateAppWidgetId(appWidgetId)

    val provider = ComponentName.unflattenFromString(data.componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateGridItemDataCache(gridItem.copy(data = newData))
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunchWidgetIntent(intent)
    }
}

private fun onDragEndPinShortcut(
    moveGridItemResult: MoveGridItemResult,
    pinItemRequest: PinItemRequest?,
    gridItem: GridItem,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        onDragEndAfterMove(
            moveGridItemResult.movingGridItem,
            moveGridItemResult.conflictingGridItem,
        )
    } else {
        onDeleteGridItemCache(gridItem)
    }
}

private fun configureComponent(
    appWidgetId: Int,
    configure: String?,
    updatedGridItem: GridItem,
    conflictingGridItem: GridItem?,
    packageManager: AndroidPackageManagerWrapper,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null && packageManager.isComponentExported(componentName = configureComponent)) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEndAfterMove(
            updatedGridItem,
            conflictingGridItem,
        )
    }
}

private fun bindPinWidget(
    appWidgetId: Int,
    gridItem: GridItem,
    pinItemRequest: PinItemRequest,
    updatedGridItem: GridItem,
    conflictingGridItem: GridItem?,
    onDragEndAfterMove: (
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    val bindPinWidget =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest.isValid &&
                pinItemRequest.accept(extras)

    if (bindPinWidget) {
        onDragEndAfterMove(
            updatedGridItem,
            conflictingGridItem,
        )
    } else {
        onDeleteGridItemCache(gridItem)
    }
}

private suspend fun onDragEndShortcutConfigActivity(
    gridItem: GridItem,
    data: GridItemData.ShortcutConfigActivity,
    userManagerWrapper: AndroidUserManagerWrapper,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    onLaunchShortcutConfigActivityIntent: (Intent) -> Unit,
    onLaunchShortcutConfigActivityIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val serialNumber =
        userManagerWrapper.getSerialNumberForUser(userHandle = Process.myUserHandle())

    if (serialNumber == data.serialNumber) {
        val intent = Intent(Intent.ACTION_CREATE_SHORTCUT).setComponent(
            ComponentName.unflattenFromString(data.componentName)
        )

        try {
            onLaunchShortcutConfigActivityIntent(intent)
        } catch (_: ActivityNotFoundException) {
            onDeleteGridItemCache(gridItem)
        }
    } else {
        val shortcutConfigActivityIntent = launcherAppsWrapper.getShortcutConfigActivityIntent(
            serialNumber = serialNumber,
            packageName = data.packageName,
            componentName = data.componentName
        )

        if (shortcutConfigActivityIntent != null) {
            val intentSenderRequest = IntentSenderRequest
                .Builder(shortcutConfigActivityIntent)
                .build()

            onLaunchShortcutConfigActivityIntentSenderRequest(intentSenderRequest)
        } else {
            onDeleteGridItemCache(gridItem)
        }
    }
}