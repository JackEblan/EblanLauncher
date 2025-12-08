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
import android.content.pm.LauncherApps
import android.content.pm.LauncherApps.PinItemRequest
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Process
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.framework.bytearray.AndroidByteArrayWrapper
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
    onLaunchShortcutConfigIntent: (Intent) -> Unit,
    onLaunchShortcutConfigIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
    onDragCancelAfterMove: () -> Unit,
    onUpdateWidgetGridItemDataCache: (GridItem) -> Unit,
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
                        onUpdateWidgetGridItemDataCache = onUpdateWidgetGridItemDataCache,
                        onUpdateAppWidgetId = onUpdateAppWidgetId,
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    onDragEndShortcutConfig(
                        gridItem = gridItemSource.gridItem,
                        data = data,
                        userManagerWrapper = userManagerWrapper,
                        launcherAppsWrapper = launcherAppsWrapper,
                        onLaunchShortcutConfigIntent = onLaunchShortcutConfigIntent,
                        onLaunchShortcutConfigIntentSenderRequest = onLaunchShortcutConfigIntentSenderRequest,
                        onDeleteGridItemCache = onDeleteGridItemCache,
                    )
                }

                else -> {
                    onDragEndAfterMove(moveGridItemResult)
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
                        onUpdateWidgetGridItemDataCache = onUpdateWidgetGridItemDataCache,
                        onUpdateAppWidgetId = onUpdateAppWidgetId,
                    )
                }

                else -> Unit
            }
        }

        else -> {
            onDragEndAfterMove(moveGridItemResult)
        }
    }
}

internal fun handleAppWidgetLauncherResult(
    result: ActivityResult,
    gridItem: GridItem,
    onUpdateWidgetGridItemDataCache: (GridItem) -> Unit,
    onDeleteAppWidgetId: () -> Unit,
) {
    val data = (gridItem.data as? GridItemData.Widget)
        ?: error("Expected GridItemData.Widget")

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateWidgetGridItemDataCache(gridItem.copy(data = newData))
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
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
) {
    requireNotNull(moveGridItemResult)

    requireNotNull(updatedGridItem)

    val data = (updatedGridItem.data as? GridItemData.Widget)
        ?: error("Expected GridItemData.Widget")

    if (resultCode == Activity.RESULT_OK) {
        onDragEndAfterMove(moveGridItemResult.copy(movingGridItem = updatedGridItem))
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
    updatedWidgetGridItem: GridItem?,
    moveGridItemResult: MoveGridItemResult?,
    packageManager: AndroidPackageManagerWrapper,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val data = (updatedWidgetGridItem?.data as? GridItemData.Widget) ?: return

    requireNotNull(moveGridItemResult)

    when (gridItemSource) {
        is GridItemSource.New -> {
            configureComponent(
                appWidgetId = data.appWidgetId,
                configure = data.configure,
                updatedGridItem = updatedWidgetGridItem,
                moveGridItemResult = moveGridItemResult,
                packageManager = packageManager,
                onConfigure = onConfigure,
                onDragEndAfterMove = onDragEndAfterMove,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                appWidgetId = data.appWidgetId,
                pinItemRequest = gridItemSource.pinItemRequest,
                updatedGridItem = updatedWidgetGridItem,
                moveGridItemResult = moveGridItemResult,
                onDragEndAfterMove = onDragEndAfterMove,
                onDeleteGridItemCache = onDeleteGridItemCache,
            )
        }

        else -> Unit
    }
}

@Suppress("DEPRECATION")
internal suspend fun handleShortcutConfigLauncherResult(
    androidByteArrayWrapper: AndroidByteArrayWrapper,
    moveGridItemResult: MoveGridItemResult?,
    result: ActivityResult,
    gridItemSource: GridItemSource,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateShortcutConfigGridItemDataCache: (
        byteArray: ByteArray?,
        moveGridItemResult: MoveGridItemResult,
        gridItem: GridItem,
        data: GridItemData.ShortcutConfig,
    ) -> Unit,
) {
    requireNotNull(moveGridItemResult)

    if (result.resultCode == Activity.RESULT_CANCELED) {
        onDeleteGridItemCache(gridItemSource.gridItem)

        return
    }

    val name = result.data?.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)

    val icon = result.data?.let { intent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Intent.EXTRA_SHORTCUT_ICON,
                Bitmap::class.java,
            )
        } else {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON)
        }
    }?.let { bitmap ->
        androidByteArrayWrapper.createByteArray(bitmap = bitmap)
    }

    val shortcutIntentUri = result.data?.let { intent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                Intent.EXTRA_SHORTCUT_INTENT,
                Intent::class.java,
            )
        } else {
            intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT)
        }
    }?.toUri(Intent.URI_INTENT_SCHEME)

    val data = (gridItemSource.gridItem.data as? GridItemData.ShortcutConfig)
        ?: error("Expected GridItemData.ShortcutConfig")

    onUpdateShortcutConfigGridItemDataCache(
        icon,
        moveGridItemResult,
        gridItemSource.gridItem,
        data.copy(
            shortcutIntentName = name,
            shortcutIntentUri = shortcutIntentUri,
        ),
    )
}

@Suppress("DEPRECATION")
internal suspend fun handleShortcutConfigIntentSenderLauncherResult(
    moveGridItemResult: MoveGridItemResult?,
    result: ActivityResult,
    userManagerWrapper: AndroidUserManagerWrapper,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    byteArrayWrapper: AndroidByteArrayWrapper,
    gridItemSource: GridItemSource,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateShortcutConfigIntoShortcutInfoGridItem: (
        moveGridItemResult: MoveGridItemResult,
        pinItemRequestType: PinItemRequestType.ShortcutInfo,
    ) -> Unit,
) {
    requireNotNull(moveGridItemResult)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
        result.resultCode == Activity.RESULT_CANCELED
    ) {
        onDeleteGridItemCache(gridItemSource.gridItem)

        return
    }

    val pinItemRequest = result.data?.let { intent ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(
                LauncherApps.EXTRA_PIN_ITEM_REQUEST,
                PinItemRequest::class.java,
            )
        } else {
            intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST)
        }
    }

    val shortcutInfo = pinItemRequest?.shortcutInfo

    if (pinItemRequest != null &&
        shortcutInfo != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        val pinItemRequestType = PinItemRequestType.ShortcutInfo(
            serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = shortcutInfo.userHandle),
            shortcutId = shortcutInfo.id,
            packageName = shortcutInfo.`package`,
            shortLabel = shortcutInfo.shortLabel.toString(),
            longLabel = shortcutInfo.longLabel.toString(),
            isEnabled = shortcutInfo.isEnabled,
            disabledMessage = shortcutInfo.disabledMessage?.toString(),
            icon = launcherAppsWrapper.getShortcutIconDrawable(
                shortcutInfo = shortcutInfo,
                density = 0,
            )?.let {
                byteArrayWrapper.createByteArray(drawable = it)
            },
        )

        onUpdateShortcutConfigIntoShortcutInfoGridItem(
            moveGridItemResult,
            pinItemRequestType,
        )
    } else {
        onDeleteGridItemCache(gridItemSource.gridItem)
    }
}

private fun onDragEndWidget(
    gridItem: GridItem,
    data: GridItemData.Widget,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    onLaunchWidgetIntent: (Intent) -> Unit,
    onUpdateWidgetGridItemDataCache: (GridItem) -> Unit,
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

        onUpdateWidgetGridItemDataCache(gridItem.copy(data = newData))
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
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        onDragEndAfterMove(moveGridItemResult)
    } else {
        onDeleteGridItemCache(gridItem)
    }
}

private fun configureComponent(
    appWidgetId: Int,
    configure: String?,
    updatedGridItem: GridItem,
    moveGridItemResult: MoveGridItemResult,
    packageManager: AndroidPackageManagerWrapper,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null &&
        packageManager.isComponentExported(componentName = configureComponent)
    ) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEndAfterMove(moveGridItemResult.copy(movingGridItem = updatedGridItem))
    }
}

private fun bindPinWidget(
    appWidgetId: Int,
    pinItemRequest: PinItemRequest,
    updatedGridItem: GridItem,
    moveGridItemResult: MoveGridItemResult,
    onDragEndAfterMove: (MoveGridItemResult) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest.isValid &&
        pinItemRequest.accept(extras)
    ) {
        onDragEndAfterMove(moveGridItemResult.copy(movingGridItem = updatedGridItem))
    } else {
        onDeleteGridItemCache(updatedGridItem)
    }
}

private suspend fun onDragEndShortcutConfig(
    gridItem: GridItem,
    data: GridItemData.ShortcutConfig,
    userManagerWrapper: AndroidUserManagerWrapper,
    launcherAppsWrapper: AndroidLauncherAppsWrapper,
    onLaunchShortcutConfigIntent: (Intent) -> Unit,
    onLaunchShortcutConfigIntentSenderRequest: (IntentSenderRequest) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val serialNumber =
        userManagerWrapper.getSerialNumberForUser(userHandle = Process.myUserHandle())

    if (serialNumber == data.serialNumber) {
        val intent = Intent(Intent.ACTION_CREATE_SHORTCUT).setComponent(
            ComponentName.unflattenFromString(data.componentName),
        )

        try {
            onLaunchShortcutConfigIntent(intent)
        } catch (_: ActivityNotFoundException) {
            onDeleteGridItemCache(gridItem)
        }
    } else {
        val shortcutConfigIntent = launcherAppsWrapper.getShortcutConfigIntent(
            serialNumber = data.serialNumber,
            packageName = data.packageName,
            componentName = data.componentName,
        )

        if (shortcutConfigIntent != null) {
            val intentSenderRequest = IntentSenderRequest
                .Builder(shortcutConfigIntent)
                .build()

            onLaunchShortcutConfigIntentSenderRequest(intentSenderRequest)
        } else {
            onDeleteGridItemCache(gridItem)
        }
    }
}
