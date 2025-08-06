package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.pager.PagerState
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

fun handleOnDragEnd(
    targetPage: Int,
    moveGridItemResult: MoveGridItemResult?,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    gridItemSource: GridItemSource,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onMoveGridItemsFailed: (Int) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
) {
    if (moveGridItemResult?.gridItems == null) {
        onMoveGridItemsFailed(targetPage)

        return
    }

    when (gridItemSource) {
        is GridItemSource.New -> {
            val data = gridItemSource.gridItem.data

            if (data is GridItemData.Widget) {
                val appWidgetId = androidAppWidgetHostWrapper.allocateAppWidgetId()

                onUpdateAppWidgetId(appWidgetId)

                onDragEndWidget(
                    gridItem = gridItemSource.gridItem,
                    data = data,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    onLaunch = onLaunch,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                )
            } else {
                onDragEndAfterMove(
                    targetPage,
                    moveGridItemResult.movingGridItem,
                    moveGridItemResult.conflictingGridItem,
                )
            }
        }

        is GridItemSource.Pin -> {
            when (val data = gridItemSource.gridItem.data) {
                is GridItemData.ShortcutInfo -> {
                    onDragEndPinShortcut(
                        pinItemRequest = gridItemSource.pinItemRequest,
                        gridItem = gridItemSource.gridItem,
                        onDeleteGridItemCache = onDeleteGridItemCache,
                    )

                    onDragEndAfterMove(
                        targetPage,
                        moveGridItemResult.movingGridItem,
                        moveGridItemResult.conflictingGridItem,
                    )
                }

                is GridItemData.Widget -> {
                    val appWidgetId = androidAppWidgetHostWrapper.allocateAppWidgetId()

                    onUpdateAppWidgetId(appWidgetId)

                    onDragEndWidget(
                        gridItem = gridItemSource.gridItem,
                        data = data,
                        appWidgetId = appWidgetId,
                        appWidgetManager = appWidgetManager,
                        onLaunch = onLaunch,
                        onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    )
                }

                else -> Unit
            }
        }

        else -> {
            onDragEndAfterMove(
                targetPage,
                moveGridItemResult.movingGridItem,
                moveGridItemResult.conflictingGridItem,
            )
        }
    }
}

fun handleAppWidgetLauncherResult(
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

fun handleConfigureResult(
    moveGridItemResult: MoveGridItemResult?,
    updatedGridItem: GridItem?,
    resultCode: Int,
    horizontalPagerState: PagerState,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDeleteWidgetGridItemCache: (
        targetPage: Int,
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
) {
    requireNotNull(moveGridItemResult)

    requireNotNull(updatedGridItem)

    val data = (updatedGridItem.data as? GridItemData.Widget)
        ?: error("Expected GridItemData.Widget")

    val (gridItems, _, conflictingGridItem) = moveGridItemResult

    requireNotNull(gridItems)

    val targetPage = calculatePage(
        index = horizontalPagerState.currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (resultCode == Activity.RESULT_CANCELED) {
        onDeleteWidgetGridItemCache(targetPage, updatedGridItem, data.appWidgetId)
    }

    onDragEndAfterMove(
        targetPage,
        updatedGridItem,
        conflictingGridItem,
    )
}

fun handleDeleteAppWidgetId(
    gridItem: GridItem,
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDeleteWidgetGridItemCache: (
        targetPage: Int,
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
) {
    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
        deleteAppWidgetId
    ) {
        check(gridItem.data is GridItemData.Widget)

        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        onDeleteWidgetGridItemCache(targetPage, gridItem, appWidgetId)
    }
}

fun handleBoundWidget(
    gridItemSource: GridItemSource,
    updatedGridItem: GridItem?,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    moveGridItemResult: MoveGridItemResult?,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val data = (updatedGridItem?.data as? GridItemData.Widget) ?: return

    requireNotNull(moveGridItemResult)

    val (gridItems, _, conflictingGridItem) = moveGridItemResult

    requireNotNull(gridItems)

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    when (gridItemSource) {
        is GridItemSource.New -> {
            configureComponent(
                targetPage = targetPage,
                appWidgetId = data.appWidgetId,
                configure = data.configure,
                updatedGridItem = updatedGridItem,
                conflictingGridItem = conflictingGridItem,
                onConfigure = onConfigure,
                onDragEndAfterMove = onDragEndAfterMove,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                targetPage = targetPage,
                appWidgetId = data.appWidgetId,
                gridItem = updatedGridItem,
                pinItemRequest = gridItemSource.pinItemRequest,
                updatedGridItem = updatedGridItem,
                conflictingGridItem = conflictingGridItem,
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
    appWidgetId: Int,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    onLaunch: (Intent) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
) {
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

        onLaunch(intent)
    }
}

private fun onDragEndPinShortcut(
    pinItemRequest: PinItemRequest?,
    gridItem: GridItem,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        return
    }

    onDeleteGridItemCache(gridItem)
}

private fun configureComponent(
    targetPage: Int,
    appWidgetId: Int,
    configure: String?,
    updatedGridItem: GridItem,
    conflictingGridItem: GridItem?,
    onConfigure: (Intent) -> Unit,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEndAfterMove(
            targetPage,
            updatedGridItem,
            conflictingGridItem,
        )
    }
}

private fun bindPinWidget(
    targetPage: Int,
    appWidgetId: Int,
    gridItem: GridItem,
    pinItemRequest: PinItemRequest,
    updatedGridItem: GridItem,
    conflictingGridItem: GridItem?,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    val bindPinWidget =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest.isValid && pinItemRequest.accept(
            extras,
        )

    if (bindPinWidget) {
        onDragEndAfterMove(
            targetPage,
            updatedGridItem,
            conflictingGridItem,
        )
    } else {
        onDeleteGridItemCache(gridItem)
    }
}