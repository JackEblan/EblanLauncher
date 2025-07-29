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
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper

fun onDroppedNew(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    drag: Drag,
    gridItemSource: GridItemSource?,
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    onDragCancel: () -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        gridItemSource: GridItemSource,
        appWidgetId: Int,
    ) -> Unit,
) {
    if (drag == Drag.End) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        onDragEndNew(
            targetPage = targetPage,
            movedGridItems = movedGridItems,
            appWidgetHostWrapper = appWidgetHostWrapper,
            appWidgetManager = appWidgetManager,
            gridItemSource = gridItemSource,
            onLaunch = onLaunch,
            onDragEnd = onDragEnd,
            onDeleteGridItem = onDeleteGridItem,
            onUpdateAppWidgetId = onUpdateAppWidgetId,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
        )

        return
    }

    if (drag == Drag.Cancel) {
        onDragCancel()
    }
}

fun handleAppWidgetLauncherResult(
    result: ActivityResult,
    gridItemSource: GridItemSource?,
    onUpdateWidgetGridItem: (
        gridItemSource: GridItemSource,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    when (gridItemSource) {
        is GridItemSource.New -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                onUpdateWidgetGridItem(gridItemSource, appWidgetId)
            } else {
                onDeleteGridItem(gridItemSource.gridItem)
            }
        }

        is GridItemSource.Pin -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                onUpdateWidgetGridItem(gridItemSource, appWidgetId)
            } else {
                onDeleteGridItem(gridItemSource.gridItem)
            }
        }

        else -> Unit
    }
}

fun handleConfigureLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItemSource: GridItemSource?,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItemSource !is GridItemSource.New) return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (result.resultCode == Activity.RESULT_CANCELED) {
        onDeleteGridItem(gridItemSource.gridItem)
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
    onDeleteWidgetGridItem: (
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItem != null &&
        appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
        deleteAppWidgetId
    ) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        onDeleteWidgetGridItem(gridItem, appWidgetId)

        onDragEnd(targetPage)
    }
}

fun handleBoundWidgetSource(
    boundWidgetSource: GridItemSource?,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    appWidgetId: Int,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val gridItem = boundWidgetSource?.gridItem ?: return

    val data = (gridItem.data as? GridItemData.Widget) ?: return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    when (boundWidgetSource) {
        is GridItemSource.New -> {
            configureComponent(
                targetPage = targetPage,
                appWidgetId = appWidgetId,
                configure = data.configure,
                onConfigure = onConfigure,
                onDragEnd = onDragEnd,
            )
        }

        is GridItemSource.Pin -> {
            bindPinWidget(
                appWidgetId = appWidgetId,
                gridItem = gridItem,
                pinItemRequest = boundWidgetSource.pinItemRequest,
                onDragEnd = onDragEnd,
                targetPage = targetPage,
                onDeleteGridItem = onDeleteGridItem,
            )
        }

        else -> Unit
    }
}

private fun onDragEndNew(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    gridItemSource: GridItemSource?,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onUpdateWidgetGridItem: (
        gridItemSource: GridItemSource,
        appWidgetId: Int,
    ) -> Unit,
) {
    when (gridItemSource) {
        is GridItemSource.New -> {
            if (!movedGridItems) {
                onDeleteGridItem(gridItemSource.gridItem)

                onDragEnd(targetPage)

                return
            }

            val data = gridItemSource.gridItem.data

            if (data is GridItemData.Widget) {
                val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

                onUpdateAppWidgetId(appWidgetId)

                onDragEndGridItemWidget(
                    gridItemSource = gridItemSource,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    componentName = data.componentName,
                    onLaunch = onLaunch,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                )
            } else {
                onDragEnd(targetPage)
            }
        }

        is GridItemSource.Pin -> {
            if (!movedGridItems) {
                onDeleteGridItem(gridItemSource.gridItem)

                onDragEnd(targetPage)

                return
            }

            when (val data = gridItemSource.gridItem.data) {
                is GridItemData.ShortcutInfo -> {
                    onDragEndPinShortcut(
                        pinItemRequest = gridItemSource.pinItemRequest,
                        gridItem = gridItemSource.gridItem,
                        onDeleteGridItem = onDeleteGridItem,
                    )

                    onDragEnd(targetPage)
                }

                is GridItemData.Widget -> {
                    val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

                    onUpdateAppWidgetId(appWidgetId)

                    onDragEndPinWidget(
                        appWidgetId = appWidgetId,
                        appWidgetManager = appWidgetManager,
                        gridItemSource = gridItemSource,
                        componentName = data.componentName,
                        onLaunch = onLaunch,
                        onUpdateWidgetGridItem = onUpdateWidgetGridItem,
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

private fun onDragEndGridItemWidget(
    gridItemSource: GridItemSource,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    componentName: String,
    onLaunch: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        gridItemSource: GridItemSource,
        appWidgetId: Int,
    ) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        onUpdateWidgetGridItem(gridItemSource, appWidgetId)
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
}

private fun onDragEndPinWidget(
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    gridItemSource: GridItemSource,
    componentName: String,
    onLaunch: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        gridItemSource: GridItemSource,
        appWidgetId: Int,
    ) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        onUpdateWidgetGridItem(gridItemSource, appWidgetId)
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
    onDeleteGridItem: (GridItem) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        return
    }

    onDeleteGridItem(gridItem)
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
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    val bindPinWidget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            pinItemRequest.isValid &&
            pinItemRequest.accept(extras)

    if (bindPinWidget) {
        onDragEnd(targetPage)
    } else {
        onDeleteGridItem(gridItem)
    }
}