package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.compose.ui.unit.IntOffset
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import kotlinx.coroutines.delay

fun handleDragEndExisting(
    drag: Drag,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDragEnd: (Int) -> Unit,
    onDragCancel: () -> Unit,
) {
    if (drag == Drag.End) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        onDragEnd(targetPage)

        return
    }

    if (drag == Drag.Cancel) {
        onDragCancel()
    }
}

fun handleDragEndNew(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    drag: Drag,
    gridItemSource: GridItemSource?,
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    onDragCancel: () -> Unit,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
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
            onConfigure = onConfigure,
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

suspend fun handlePageDirection(
    currentPage: Int,
    pageDirection: PageDirection?,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    when (pageDirection) {
        PageDirection.Left -> {
            onAnimateScrollToPage(currentPage - 1)
        }

        PageDirection.Right -> {
            onAnimateScrollToPage(currentPage + 1)
        }

        null -> Unit
    }
}

suspend fun handleDragIntOffset(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    gridItems: Map<Int, List<GridItem>>,
    dockGridItems: List<GridItem>,
    drag: Drag,
    gridItem: GridItem?,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    dockHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    dockColumns: Int,
    dockRows: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveGridItem: (
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    if (drag != Drag.Dragging ||
        gridItem == null ||
        isScrollInProgress
    ) {
        return
    }

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val gridItemsByPage = gridItems[targetPage].orEmpty()

    val isDraggingOnDock =
        dragIntOffset.y > (rootHeight - dockHeight) - gridPadding

    val delay = 500L

    if (dragIntOffset.x <= gridPadding && !isDraggingOnDock) {
        delay(delay)

        onUpdatePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
        delay(delay)

        onUpdatePageDirection(PageDirection.Right)
    } else if (isDraggingOnDock) {
        delay(delay)

        val cellWidth = rootWidth / dockColumns

        val cellHeight = dockHeight / dockRows

        val dockY = dragIntOffset.y - (rootHeight - dockHeight)

        val newGridItem = gridItem.copy(
            page = targetPage,
            startRow = dockY / cellHeight,
            startColumn = dragIntOffset.x / cellWidth,
            associate = Associate.Dock,
        )

        if (isGridItemSpanWithinBounds(
                gridItem = newGridItem,
                rows = dockRows,
                columns = dockColumns,
            )
        ) {
            onMoveGridItem(
                dockGridItems,
                newGridItem,
                dragIntOffset.x,
                dockY,
                dockRows,
                dockColumns,
                rootWidth,
                dockHeight,
            )
        }
    } else {
        delay(delay)

        val gridWidth = rootWidth - (gridPadding * 2)

        val gridHeight = (rootHeight - dockHeight) - (gridPadding * 2)

        val gridX = dragIntOffset.x - gridPadding

        val gridY = dragIntOffset.y - gridPadding

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val newGridItem = gridItem.copy(
            page = targetPage,
            startRow = gridY / cellHeight,
            startColumn = gridX / cellWidth,
            associate = Associate.Grid,
        )

        if (isGridItemSpanWithinBounds(
                gridItem = newGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            onMoveGridItem(
                gridItemsByPage,
                newGridItem,
                gridX,
                gridY,
                rows,
                columns,
                gridWidth,
                gridHeight,
            )
        }
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

fun handleAppWidgetLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItemSource: GridItemSource?,
    onDragEnd: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val data = (gridItemSource?.gridItem?.data as? GridItemData.Widget) ?: return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    when (gridItemSource) {
        is GridItemSource.New -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                onUpdateWidgetGridItem(gridItemSource.gridItem.id, appWidgetId)

                configureComponent(
                    targetPage = targetPage,
                    appWidgetId = appWidgetId,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onDragEnd = onDragEnd,
                )
            } else {
                onDeleteGridItem(gridItemSource.gridItem)
            }
        }

        is GridItemSource.Pin -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                onUpdateWidgetGridItem(gridItemSource.gridItem.id, appWidgetId)

                bindWidget(
                    appWidgetId = appWidgetId,
                    gridItem = gridItemSource.gridItem,
                    pinItemRequest = gridItemSource.pinItemRequest,
                    onDragEnd = onDragEnd,
                    targetPage = targetPage,
                    onDeleteGridItem = onDeleteGridItem,
                )
            } else {
                onDeleteGridItem(gridItemSource.gridItem)
            }
        }

        else -> Unit
    }
}

fun handleDeleteAppWidgetId(
    gridItem: GridItem?,
    appWidgetId: Int,
    deleteAppWidgetId: Boolean,
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    onDeleteWidgetGridItem: (gridItem: GridItem, appWidgetId: Int) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItem == null) return

    if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID &&
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

private fun bindWidget(
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

private fun onDragEndNew(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    gridItemSource: GridItemSource?,
    onConfigure: (Intent) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateAppWidgetId: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
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
                    id = gridItemSource.gridItem.id,
                    targetPage = targetPage,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    componentName = data.componentName,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onLaunch = onLaunch,
                    onDragEnd = onDragEnd,
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
                        targetPage = targetPage,
                        appWidgetId = appWidgetId,
                        appWidgetManager = appWidgetManager,
                        gridItem = gridItemSource.gridItem,
                        componentName = data.componentName,
                        pinItemRequest = gridItemSource.pinItemRequest,
                        onLaunch = onLaunch,
                        onDragEnd = onDragEnd,
                        onDeleteGridItem = onDeleteGridItem,
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

private fun onDragEndGridItemWidget(
    id: Int,
    targetPage: Int,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    componentName: String,
    configure: String?,
    onConfigure: (Intent) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        onUpdateWidgetGridItem(id, appWidgetId)

        configureComponent(
            targetPage = targetPage,
            appWidgetId = appWidgetId,
            configure = configure,
            onConfigure = onConfigure,
            onDragEnd = onDragEnd,
        )

        onDragEnd(targetPage)
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
}

private fun onDragEndPinWidget(
    targetPage: Int,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    gridItem: GridItem,
    componentName: String,
    pinItemRequest: PinItemRequest,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        bindWidget(
            appWidgetId = appWidgetId,
            gridItem = gridItem,
            pinItemRequest = pinItemRequest,
            onDragEnd = onDragEnd,
            targetPage = targetPage,
            onDeleteGridItem = onDeleteGridItem,
        )
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        onLaunch(intent)
    }
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