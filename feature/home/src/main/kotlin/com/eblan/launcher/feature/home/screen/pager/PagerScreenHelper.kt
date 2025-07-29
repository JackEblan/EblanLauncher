package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.pm.LauncherApps.PinItemRequest
import android.content.pm.ShortcutInfo
import android.os.Build
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import com.eblan.launcher.common.util.toByteArray
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.getShortcutGridItem
import com.eblan.launcher.domain.grid.getWidgetGridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.launcherapps.LauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.wallpapermanager.WallpaperManagerWrapper
import kotlinx.coroutines.flow.onStart
import java.io.File

suspend fun handlePinItemRequest(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    rows: Int,
    columns: Int,
    gridWidth: Int,
    gridHeight: Int,
    drag: Drag,
    pinItemRequestWrapper: PinItemRequestWrapper,
    launcherAppsWrapper: LauncherAppsWrapper,
    context: Context,
    fileManager: FileManager,
    onDragStart: (GridItemSource) -> Unit,
) {
    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

    suspend fun getWidgetGridItemSource(
        pinItemRequest: PinItemRequest,
        appWidgetProviderInfo: AppWidgetProviderInfo,
    ): GridItemSource {
        val byteArray = appWidgetProviderInfo.loadPreviewImage(context, 0)?.toByteArray()

        val previewInferred = File(
            fileManager.widgetsDirectory,
            appWidgetProviderInfo.provider.className,
        ).absolutePath

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    page = targetPage,
                    rows = rows,
                    columns = columns,
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
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    preview = previewInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
            )
        } else {
            GridItemSource.Pin(
                gridItem = getWidgetGridItem(
                    page = targetPage,
                    rows = rows,
                    columns = columns,
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
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    preview = previewInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
            )
        }
    }

    suspend fun getShortcutGridItemSource(
        pinItemRequest: PinItemRequest,
        shortcutInfo: ShortcutInfo,
    ): GridItemSource? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val byteArray = launcherAppsWrapper.getShortcutIconDrawable(
                shortcutInfo = shortcutInfo,
                density = 0,
            ).toByteArray()

            val iconInferred = File(fileManager.shortcutsDirectory, shortcutInfo.id).absolutePath

            GridItemSource.Pin(
                gridItem = getShortcutGridItem(
                    page = targetPage,
                    id = shortcutInfo.id,
                    packageName = shortcutInfo.`package`,
                    shortLabel = shortcutInfo.shortLabel.toString(),
                    longLabel = shortcutInfo.longLabel.toString(),
                    icon = iconInferred,
                ),
                pinItemRequest = pinItemRequest,
                byteArray = byteArray,
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

suspend fun handleWallpaperScroll(
    horizontalPagerState: PagerState,
    wallpaperScroll: Boolean,
    wallpaperManagerWrapper: WallpaperManagerWrapper,
    pageCount: Int,
    infiniteScroll: Boolean,
    windowToken: android.os.IBinder,
) {
    if (!wallpaperScroll) return

    snapshotFlow {
        horizontalPagerState.currentPage to horizontalPagerState.currentPageOffsetFraction
    }.onStart {
        wallpaperManagerWrapper.setWallpaperOffsetSteps(
            xStep = 1f / (pageCount - 1),
            yStep = 1f,
        )
    }.collect { (currentPage, offsetFraction) ->
        val page = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        val scrollProgress = (page + offsetFraction).coerceIn(0f, (pageCount - 1).toFloat())

        val xOffset = scrollProgress / (pageCount - 1)

        wallpaperManagerWrapper.setWallpaperOffsets(
            windowToken = windowToken,
            xOffset = xOffset,
            yOffset = 0f,
        )
    }
}