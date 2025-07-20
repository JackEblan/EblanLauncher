package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    movedGridItems: Boolean,
    pinItemRequest: PinItemRequest?,
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
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    val horizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + currentPage else currentPage,
        pageCount = {
            if (infiniteScroll) {
                Int.MAX_VALUE
            } else {
                pageCount
            }
        },
    )

    val horizontalPagerPaddingDp = 50.dp

    val gridPaddingDp = 8.dp

    val gridPaddingPx = with(density) {
        (horizontalPagerPaddingDp + gridPaddingDp).roundToPx()
    }

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            result = result,
            gridItem = gridItemSource?.gridItemLayoutInfo?.gridItem,
            onDragEnd = onDragEnd,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onDeleteGridItem = onDeleteGridItem,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            result = result,
            type = gridItemSource?.type,
            gridItem = gridItemSource?.gridItemLayoutInfo?.gridItem,
            pinItemRequest = pinItemRequest,
            onDragEnd = onDragEnd,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onDeleteGridItem = onDeleteGridItem,
            onConfigure = configureLauncher::launch,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            gridItems = gridItems,
            dockGridItems = dockGridItems,
            drag = drag,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
            dragIntOffset = dragIntOffset,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            gridPadding = gridPaddingPx,
            rootWidth = rootWidth,
            dockColumns = dockColumns,
            dockRows = dockRows,
            columns = columns,
            rows = rows,
            isScrollInProgress = horizontalPagerState.isScrollInProgress,
            onChangePageDirection = { newPageDirection ->
                pageDirection = newPageDirection
            },
            onMoveGridItem = onMoveGridItem,
        )
    }

    LaunchedEffect(key1 = pageDirection) {
        handlePageDirection(
            currentPage = horizontalPagerState.currentPage,
            pageDirection = pageDirection,
            onAnimateScrollToPage = { page ->
                horizontalPagerState.animateScrollToPage(page = page)

                pageDirection = null
            },
        )
    }

    LaunchedEffect(key1 = drag) {
        handleDrag(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            drag = drag,
            gridItemSource = gridItemSource,
            pinItemRequest = pinItemRequest,
            onDragEnd = onDragEnd,
            movedGridItems = movedGridItems,
            appWidgetManager = appWidgetManager,
            appWidgetLauncher = appWidgetLauncher,
            onDragCancel = onDragCancel,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onConfigure = configureLauncher::launch,
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(all = horizontalPagerPaddingDp),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            GridLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(gridPaddingDp)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .background(color = Color.White.copy(alpha = 0.25f)),
                rows = rows,
                columns = columns,
            ) {
                gridItems[page]?.forEach { gridItem ->
                    key(gridItem.id) {
                        LookaheadScope {
                            val gridItemModifier = Modifier
                                .animateBounds(this)
                                .gridItem(gridItem)

                            when (val data = gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    ApplicationInfoGridItem(
                                        modifier = gridItemModifier,
                                        data = data,
                                        color = color,
                                    )
                                }

                                is GridItemData.Widget -> {
                                    WidgetGridItem(
                                        modifier = gridItemModifier,
                                        data = data,
                                    )
                                }

                                is GridItemData.ShortcutInfo -> {
                                    ShortcutInfoGridItem(
                                        modifier = gridItemModifier,
                                        data = data,
                                        color = color,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
        ) {
            dockGridItems.forEach { dockGridItem ->
                key(dockGridItem.id) {
                    LookaheadScope {
                        val gridItemModifier = Modifier
                            .animateBounds(this)
                            .gridItem(dockGridItem)

                        when (val data = dockGridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                    color = color,
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                ShortcutInfoGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                    color = color,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleDrag(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    drag: Drag,
    gridItemSource: GridItemSource?,
    onDragEnd: (Int) -> Unit,
    movedGridItems: Boolean,
    appWidgetManager: AppWidgetManagerWrapper,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    pinItemRequest: PinItemRequest?,
    onDragCancel: () -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    when (drag) {
        Drag.End -> {
            handleOnDragEnd(
                targetPage = targetPage,
                movedGridItems = movedGridItems,
                appWidgetManager = appWidgetManager,
                appWidgetLauncher = appWidgetLauncher,
                type = gridItemSource?.type,
                data = gridItemSource?.gridItemLayoutInfo?.gridItem?.data,
                pinItemRequest = pinItemRequest,
                onDragEnd = onDragEnd,
                onDeleteAppWidgetId = onDeleteAppWidgetId,
                onConfigure = onConfigure,
            )
        }

        Drag.Cancel -> {
            onDragCancel()
        }

        else -> Unit
    }
}

private fun handleOnDragEnd(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetManager: AppWidgetManagerWrapper,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    type: GridItemSource.Type?,
    data: GridItemData?,
    pinItemRequest: PinItemRequest?,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    when (type) {
        GridItemSource.Type.New -> {
            when (data) {
                is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                    onDragEnd(targetPage)
                }

                is GridItemData.Widget -> {
                    onDragEndGridItemDataWidget(
                        targetPage = targetPage,
                        movedGridItems = movedGridItems,
                        appWidgetManager = appWidgetManager,
                        data = data,
                        onDragEnd = onDragEnd,
                        appWidgetLauncher = appWidgetLauncher,
                        onDeleteAppWidgetId = onDeleteAppWidgetId,
                        onConfigure = onConfigure,
                    )
                }

                null -> Unit
            }
        }

        GridItemSource.Type.Old -> {
            onDragEnd(targetPage)
        }

        GridItemSource.Type.Pin -> {
            when (data) {
                is GridItemData.Widget -> {
                    onDragEndPinWidget(
                        targetPage = targetPage,
                        movedGridItems = movedGridItems,
                        appWidgetManager = appWidgetManager,
                        data = data,
                        pinItemRequest = pinItemRequest,
                        appWidgetLauncher = appWidgetLauncher,
                        onDeleteAppWidgetId = onDeleteAppWidgetId,
                        onDragEnd = onDragEnd,
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    onDragEnd(targetPage)
                }

                else -> Unit
            }
        }

        null -> Unit
    }
}

private fun onDragEndGridItemDataWidget(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetManager: AppWidgetManagerWrapper,
    data: GridItemData.Widget,
    onDragEnd: (Int) -> Unit,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    if (!movedGridItems) {
        onDeleteAppWidgetId(data.appWidgetId)

        onDragEnd(targetPage)

        return
    }

    val provider = ComponentName.unflattenFromString(data.componentName)

    if (appWidgetManager.bindAppWidgetIdIfAllowed(
            appWidgetId = data.appWidgetId,
            provider = provider,
        )
    ) {
        configureComponent(
            appWidgetId = data.appWidgetId,
            data = data,
            targetPage = targetPage,
            onConfigure = onConfigure,
            onDragEnd = onDragEnd,
        )

        return
    }

    val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, data.appWidgetId)

        putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
    }

    appWidgetLauncher.launch(intent)
}

private fun onDragEndPinWidget(
    targetPage: Int,
    movedGridItems: Boolean,
    appWidgetManager: AppWidgetManagerWrapper,
    data: GridItemData.Widget,
    pinItemRequest: PinItemRequest?,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDeleteAppWidgetId: (Int) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (!movedGridItems) {
        onDeleteAppWidgetId(data.appWidgetId)

        handleDragEndPin(
            targetPage = targetPage,
            pinItemRequest = pinItemRequest,
            appWidgetId = data.appWidgetId,
            onDragEnd = onDragEnd,
        )

        return
    }

    val provider = ComponentName.unflattenFromString(data.componentName)

    if (appWidgetManager.bindAppWidgetIdIfAllowed(
            appWidgetId = data.appWidgetId,
            provider = provider,
        )
    ) {
        handleDragEndPin(
            targetPage = targetPage,
            pinItemRequest = pinItemRequest,
            appWidgetId = data.appWidgetId,
            onDragEnd = onDragEnd,
        )
    } else {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, data.appWidgetId)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }

        appWidgetLauncher.launch(intent)
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

private suspend fun handleDragIntOffset(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    gridItems: Map<Int, List<GridItem>>,
    dockGridItems: List<GridItem>,
    drag: Drag,
    gridItemLayoutInfo: GridItemLayoutInfo?,
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
    onChangePageDirection: (PageDirection) -> Unit,
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
    if (drag != Drag.Dragging || gridItemLayoutInfo == null || isScrollInProgress) {
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

        onChangePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
        delay(delay)

        onChangePageDirection(PageDirection.Right)
    } else if (isDraggingOnDock) {
        delay(delay)

        val cellWidth = rootWidth / dockColumns

        val cellHeight = dockHeight / dockRows

        val dockY = dragIntOffset.y - (rootHeight - dockHeight)

        val gridItem = gridItemLayoutInfo.gridItem.copy(
            page = targetPage,
            startRow = dockY / cellHeight,
            startColumn = dragIntOffset.x / cellWidth,
            associate = Associate.Dock,
        )

        onMoveGridItem(
            gridItemsByPage + dockGridItems,
            gridItem,
            dragIntOffset.x,
            dockY,
            dockRows,
            dockColumns,
            rootWidth,
            dockHeight,
        )
    } else {
        delay(delay)

        val gridWidth = rootWidth - (gridPadding * 2)

        val gridHeight = (rootHeight - dockHeight) - (gridPadding * 2)

        val gridX = dragIntOffset.x - gridPadding

        val gridY = dragIntOffset.y - gridPadding

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val gridItem = gridItemLayoutInfo.gridItem.copy(
            page = targetPage,
            startRow = gridY / cellHeight,
            startColumn = gridX / cellWidth,
            associate = Associate.Grid,
        )

        onMoveGridItem(
            gridItemsByPage + dockGridItems,
            gridItem,
            gridX,
            gridY,
            rows,
            columns,
            gridWidth,
            gridHeight,
        )
    }
}

private fun handleAppWidgetLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    type: GridItemSource.Type?,
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest?,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (result.resultCode == Activity.RESULT_OK) {
        when (type) {
            GridItemSource.Type.Old, GridItemSource.Type.New -> {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                configureComponent(
                    appWidgetId = appWidgetId,
                    data = data,
                    targetPage = targetPage,
                    onConfigure = onConfigure,
                    onDragEnd = onDragEnd,
                )
            }

            GridItemSource.Type.Pin -> {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        ?: -1

                handleDragEndPin(
                    targetPage = targetPage,
                    pinItemRequest = pinItemRequest,
                    appWidgetId = appWidgetId,
                    onDragEnd = onDragEnd,
                )
            }

            null -> Unit
        }
    } else {
        onDeleteAppWidgetId(data.appWidgetId)

        onDeleteGridItem(gridItem)

        onDragEnd(targetPage)
    }
}


private fun handleDragEndPin(
    targetPage: Int,
    pinItemRequest: PinItemRequest?,
    appWidgetId: Int,
    onDragEnd: (Int) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid
    ) {
        val extras = Bundle().apply {
            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        if (pinItemRequest.accept(extras)) {
            onDragEnd(targetPage)
        }
    }
}

private fun configureComponent(
    appWidgetId: Int,
    data: GridItemData.Widget,
    targetPage: Int,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val configureComponent = data.configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEnd(targetPage)
    }
}

private fun handleConfigureLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItem: GridItem?,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    if (gridItem?.data !is GridItemData.Widget) return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val appWidgetId =
        result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

    if (result.resultCode == Activity.RESULT_OK) {
        onDragEnd(targetPage)
    } else {
        onDeleteAppWidgetId(appWidgetId)

        onDeleteGridItem(gridItem)

        onDragEnd(targetPage)
    }
}