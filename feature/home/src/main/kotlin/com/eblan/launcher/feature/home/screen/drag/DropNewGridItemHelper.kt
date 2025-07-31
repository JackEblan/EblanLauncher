package com.eblan.launcher.feature.home.screen.drag

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
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper

fun handleOnDragEnd(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    movedGridItems: Boolean,
    androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper,
    appWidgetManager: AndroidAppWidgetManagerWrapper,
    gridItemSource: GridItemSource?,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
) {
    if (gridItemSource == null) return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (!movedGridItems) {
        onDeleteGridItemCache(gridItemSource.gridItem)

        onDragEnd(targetPage)

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
                onDragEnd(targetPage)
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

                    onDragEnd(targetPage)
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
            onDragEnd(targetPage)
        }
    }
}

fun handleAppWidgetLauncherResult(
    result: ActivityResult,
    gridItem: GridItem?,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteAppWidgetId: () -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        val newData = data.copy(appWidgetId = appWidgetId)

        onUpdateGridItemDataCache(gridItem.copy(data = newData))
    } else {
        onDeleteAppWidgetId()
    }
}

fun handleConfigureLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItem: GridItem?,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItem == null) return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (result.resultCode == Activity.RESULT_CANCELED) {
        onDeleteGridItemCache(gridItem)
    }

    onDragEnd(targetPage)
}

fun handleDeleteAppWidgetId(
    gridItem: GridItem?,
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDeleteGridItemCache: (gridItem: GridItem) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID && deleteAppWidgetId) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        val newData = data.copy(appWidgetId = appWidgetId)

        onDeleteGridItemCache(gridItem.copy(data = newData))

        onDragEnd(targetPage)
    }
}

fun handleBoundWidget(
    gridItemSource: GridItemSource?,
    updatedGridItem: GridItem?,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
) {
    if (gridItemSource == null) return

    val data = (updatedGridItem?.data as? GridItemData.Widget) ?: return

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
                onConfigure = onConfigure,
                onDragEnd = onDragEnd,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                appWidgetId = data.appWidgetId,
                gridItem = updatedGridItem,
                pinItemRequest = gridItemSource.pinItemRequest,
                onDragEnd = onDragEnd,
                targetPage = targetPage,
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && pinItemRequest != null && pinItemRequest.isValid && pinItemRequest.accept()) {
        return
    }

    onDeleteGridItemCache(gridItem)
}

private fun configureComponent(
    targetPage: Int,
    appWidgetId: Int,
    configure: String?,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEnd(targetPage)
    }
}

private fun bindPinWidget(
    appWidgetId: Int,
    gridItem: GridItem,
    pinItemRequest: PinItemRequest,
    onDragEnd: (Int) -> Unit,
    targetPage: Int,
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
        onDragEnd(targetPage)
    } else {
        onDeleteGridItemCache(gridItem)
    }
}