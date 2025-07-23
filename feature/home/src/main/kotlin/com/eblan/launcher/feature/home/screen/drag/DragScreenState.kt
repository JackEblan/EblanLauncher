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

fun handleDragExisting(
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

fun handleDragNew(
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
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    if (drag == Drag.End) {
        val targetPage = calculatePage(
            index = currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        handleOnDragEnd(
            targetPage = targetPage,
            movedGridItems = movedGridItems,
            appWidgetHostWrapper = appWidgetHostWrapper,
            appWidgetManager = appWidgetManager,
            gridItemSource = gridItemSource,
            onConfigure = onConfigure,
            onDeleteGridItem = onDeleteGridItem,
            onUpdatePinWidget = onUpdatePinWidget,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
            onLaunch = onLaunch,
            onDragEnd = onDragEnd,
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
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItemSource !is GridItemSource.New) return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val appWidgetId =
        result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

    if (result.resultCode == Activity.RESULT_OK) {
        onUpdatePinWidget(gridItemSource.gridItem.id, appWidgetId)
    } else {
        onDeleteWidgetGridItem(gridItemSource.gridItem.id)
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
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    when (gridItemSource) {
        is GridItemSource.New -> {
            val data = (gridItemSource.gridItem.data as? GridItemData.Widget) ?: return

            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                configureComponent(
                    targetPage = targetPage,
                    id = gridItemSource.gridItem.id,
                    appWidgetId = appWidgetId,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDragEnd = onDragEnd,
                )
            } else {
                onDeleteWidgetGridItem(gridItemSource.gridItem.id)

                onDragEnd(targetPage)
            }
        }

        is GridItemSource.Pin -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                handleDragEndPinWidget(
                    id = gridItemSource.gridItem.id,
                    pinItemRequest = gridItemSource.pinItemRequest,
                    appWidgetId = appWidgetId,
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                )
            } else {
                onDeleteWidgetGridItem(gridItemSource.gridItem.id)
            }

            onDragEnd(targetPage)
        }

        else -> Unit
    }
}

private fun handleOnDragEnd(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    gridItemSource: GridItemSource?,
    onConfigure: (Intent) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
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

                onUpdatePinWidget(gridItemSource.gridItem.id, appWidgetId)

                onDragEndGridItemWidget(
                    targetPage = targetPage,
                    gridItem = gridItemSource.gridItem,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    componentName = data.componentName,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onLaunch = onLaunch,
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDragEnd = onDragEnd,
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
                        id = gridItemSource.gridItem.id,
                        onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                    )

                    onDragEnd(targetPage)
                }

                is GridItemData.Widget -> {
                    val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

                    onUpdatePinWidget(gridItemSource.gridItem.id, appWidgetId)

                    onDragEndPinWidget(
                        targetPage = targetPage,
                        gridItem = gridItemSource.gridItem,
                        appWidgetId = appWidgetId,
                        appWidgetManager = appWidgetManager,
                        componentName = data.componentName,
                        pinItemRequest = gridItemSource.pinItemRequest,
                        onUpdatePinWidget = onUpdatePinWidget,
                        onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                        onLaunch = onLaunch,
                        onDragEnd = onDragEnd,
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
    id: Int,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        return
    }

    onDeleteWidgetGridItem(id)
}

private fun onDragEndGridItemWidget(
    targetPage: Int,
    gridItem: GridItem,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    componentName: String,
    configure: String?,
    onConfigure: (Intent) -> Unit,
    onLaunch: (Intent) -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        configureComponent(
            targetPage = targetPage,
            id = gridItem.id,
            appWidgetId = appWidgetId,
            configure = configure,
            onConfigure = onConfigure,
            onUpdatePinWidget = onUpdatePinWidget,
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
    gridItem: GridItem,
    appWidgetId: Int,
    appWidgetManager: AppWidgetManagerWrapper,
    componentName: String,
    pinItemRequest: PinItemRequest?,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val provider = ComponentName.unflattenFromString(componentName)

    val bindAppWidgetIdIfAllowed = appWidgetManager.bindAppWidgetIdIfAllowed(
        appWidgetId = appWidgetId,
        provider = provider,
    )

    if (bindAppWidgetIdIfAllowed) {
        handleDragEndPinWidget(
            id = gridItem.id,
            pinItemRequest = pinItemRequest,
            appWidgetId = appWidgetId,
            onUpdatePinWidget = onUpdatePinWidget,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
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

private fun handleDragEndPinWidget(
    id: Int,
    pinItemRequest: PinItemRequest?,
    appWidgetId: Int,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    val extras = Bundle().apply {
        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept(extras)
    ) {
        onUpdatePinWidget(id, appWidgetId)
    } else {
        onDeleteWidgetGridItem(id)
    }
}

private fun configureComponent(
    targetPage: Int,
    id: Int,
    appWidgetId: Int,
    configure: String?,
    onConfigure: (Intent) -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onUpdatePinWidget(id, appWidgetId)

        onDragEnd(targetPage)
    }
}