package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.widget.getWidgetGridItem
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val SwipeThreshold = 100f

@OptIn(ExperimentalUuidApi::class)
suspend fun handlePinItemRequest(
    currentPage: Int,
    drag: Drag,
    pinItemRequestWrapper: PinItemRequestWrapper,
    context: Context,
    fileManager: FileManager,
    gridItemSettings: GridItemSettings,
    onDragStart: (GridItemSource) -> Unit,
) {
    val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

    suspend fun getWidgetGridItemSource(
        pinItemRequest: PinItemRequest,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): GridItemSource {
        val id = Uuid.random().toHexString()

        val previewInferred = File(
            fileManager.getDirectory(FileManager.WIDGETS_DIR),
            appWidgetProviderInfo.provider.className,
        ).absolutePath

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    id = id,
                    page = currentPage,
                    componentName = appWidgetProviderInfo.provider.flattenToString(),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    packageName = appWidgetProviderInfo.provider.packageName,
                    targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                    preview = previewInferred,
                    gridItemSettings = gridItemSettings,
                ),
                pinItemRequest = pinItemRequest,
            )
        } else {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    id = id,
                    page = currentPage,
                    componentName = appWidgetProviderInfo.provider.flattenToString(),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    packageName = appWidgetProviderInfo.provider.packageName,
                    targetCellHeight = 0,
                    targetCellWidth = 0,
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = 0,
                    maxResizeHeight = 0,
                    preview = previewInferred,
                    gridItemSettings = gridItemSettings,
                ),
                pinItemRequest = pinItemRequest,
            )
        }
    }

    suspend fun getShortcutGridItemSource(
        pinItemRequest: PinItemRequest,
        shortcutInfo: ShortcutInfo,
    ): GridItemSource? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val iconInferred = File(
                fileManager.getDirectory(FileManager.SHORTCUTS_DIR),
                shortcutInfo.id,
            ).absolutePath

            val data = GridItemData.ShortcutInfo(
                shortcutId = shortcutInfo.id,
                packageName = shortcutInfo.`package`,
                shortLabel = shortcutInfo.shortLabel.toString(),
                longLabel = shortcutInfo.longLabel.toString(),
                icon = iconInferred,
            )

            GridItemSource.Pin(
                gridItem = GridItem(
                    id = shortcutInfo.id,
                    folderId = null,
                    page = currentPage,
                    startRow = 0,
                    startColumn = 0,
                    rowSpan = 1,
                    columnSpan = 1,
                    data = data,
                    associate = Associate.Grid,
                    override = false,
                    gridItemSettings = gridItemSettings,
                ),
                pinItemRequest = pinItemRequest,
            )
        } else {
            null
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null) {
        when (drag) {
            Drag.Start -> {
                when (pinItemRequest.requestType) {
                    PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                        val appWidgetProviderInfo = pinItemRequest.getAppWidgetProviderInfo(context)

                        if (appWidgetProviderInfo != null) {
                            onDragStart(
                                getWidgetGridItemSource(
                                    pinItemRequest = pinItemRequest,
                                    appWidgetProviderInfo = appWidgetProviderInfo,
                                ),
                            )
                        }
                    }

                    PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                        val shortcutInfo = pinItemRequest.shortcutInfo

                        if (shortcutInfo != null) {
                            getShortcutGridItemSource(
                                pinItemRequest = pinItemRequest,
                                shortcutInfo = shortcutInfo,
                            )?.let { gridItemSource ->
                                onDragStart(gridItemSource)
                            }
                        }
                    }
                }
            }

            Drag.End -> {
                pinItemRequestWrapper.updatePinItemRequest(null)
            }

            else -> Unit
        }
    }
}

fun doGestureActions(
    gestureSettings: GestureSettings,
    swipeUpY: Float,
    swipeDownY: Float,
    rootHeight: Int,
    onStartMainActivity: (String?) -> Unit,
) {
    if (swipeUpY < rootHeight - SwipeThreshold) {
        when (val gestureAction = gestureSettings.swipeUp) {
            GestureAction.None, GestureAction.OpenAppDrawer -> {
            }

            is GestureAction.OpenApp -> {
                onStartMainActivity(gestureAction.componentName)
            }

            GestureAction.OpenNotificationPanel -> {
            }
        }
    }

    if (swipeDownY < rootHeight - SwipeThreshold) {
        when (val gestureAction = gestureSettings.swipeDown) {
            GestureAction.None, GestureAction.OpenAppDrawer -> {
            }

            is GestureAction.OpenApp -> {
                onStartMainActivity(gestureAction.componentName)
            }

            GestureAction.OpenNotificationPanel -> {
            }
        }
    }
}

fun resetSwipeOffset(
    scope: CoroutineScope,
    gestureSettings: GestureSettings,
    swipeDownY: Animatable<Float, AnimationVector1D>,
    rootHeight: Int,
    swipeUpY: Animatable<Float, AnimationVector1D>,
) {
    val swipeThreshold = rootHeight - 200f

    scope.launch {
        if (gestureSettings.swipeDown is GestureAction.OpenAppDrawer) {
            val swipeDownYTarget = if (swipeDownY.value < swipeThreshold) {
                0f
            } else {
                rootHeight.toFloat()
            }

            swipeDownY.animateTo(swipeDownYTarget)
        } else {
            swipeDownY.snapTo(rootHeight.toFloat())
        }
    }

    scope.launch {
        if (gestureSettings.swipeUp is GestureAction.OpenAppDrawer) {
            val swipeUpYTarget = if (swipeUpY.value < swipeThreshold) {
                0f
            } else {
                rootHeight.toFloat()
            }
            swipeUpY.animateTo(swipeUpYTarget)
        } else {
            swipeUpY.snapTo(rootHeight.toFloat())
        }
    }
}