package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.Bundle
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
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalPinItemRequest
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewDragScreen(
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
    onDragCancel: () -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

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

    val pinItemRequestWrapper = LocalPinItemRequest.current

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            result = result,
            gridItem = gridItemSource?.gridItem,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
            onDragEnd = onDragEnd,
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
            gridItem = gridItemSource?.gridItem,
            pinItemRequest = pinItemRequestWrapper.getPinItemRequest(),
            onDragEnd = onDragEnd,
            onConfigure = configureLauncher::launch,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
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
            gridItem = gridItemSource?.gridItem,
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
            movedGridItems = movedGridItems,
            appWidgetHostWrapper = appWidgetHost,
            appWidgetManager = appWidgetManager,
            pinItemRequest = pinItemRequestWrapper.getPinItemRequest(),
            onDragCancel = onDragCancel,
            onConfigure = configureLauncher::launch,
            onDragEnd = onDragEnd,
            onDeleteGridItem = onDeleteGridItem,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
            onLaunch = appWidgetLauncher::launch,
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
    movedGridItems: Boolean,
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    pinItemRequest: PinItemRequest?,
    onDragCancel: () -> Unit,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
) {
    when (drag) {
        Drag.End -> {
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
                type = gridItemSource?.type,
                gridItem = gridItemSource?.gridItem,
                pinItemRequest = pinItemRequest,
                onConfigure = onConfigure,
                onDeleteGridItem = onDeleteGridItem,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                onLaunch = onLaunch,
                onDragEnd = onDragEnd,
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
    appWidgetHostWrapper: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerWrapper,
    type: GridItemSource.Type?,
    gridItem: GridItem?,
    pinItemRequest: PinItemRequest?,
    onConfigure: (Intent) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onLaunch: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    if (gridItem == null) {
        onDragEnd(targetPage)

        return
    }

    if (!movedGridItems) {
        onDeleteGridItem(gridItem)

        onDragEnd(targetPage)

        return
    }

    val data = gridItem.data

    val appWidgetId = appWidgetHostWrapper.allocateAppWidgetId()

    onUpdateWidgetGridItem(gridItem.id, appWidgetId)

    when (type) {
        GridItemSource.Type.New -> {
            if (data is GridItemData.Widget) {
                onDragEndGridItemWidget(
                    targetPage = targetPage,
                    gridItem = gridItem,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    componentName = data.componentName,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onLaunch = onLaunch,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onDragEnd = onDragEnd,
                )
            } else {
                onDragEnd(targetPage)
            }
        }

        GridItemSource.Type.Pin -> {
            if (data is GridItemData.Widget) {
                onDragEndPinWidget(
                    targetPage = targetPage,
                    gridItem = gridItem,
                    appWidgetId = appWidgetId,
                    appWidgetManager = appWidgetManager,
                    componentName = data.componentName,
                    pinItemRequest = pinItemRequest,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                    onLaunch = onLaunch,
                    onDragEnd = onDragEnd,
                )
            } else {
                onDragEnd(targetPage)
            }
        }

        null -> Unit
    }
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
    onUpdateWidgetGridItem: (
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
            id = gridItem.id,
            appWidgetId = appWidgetId,
            configure = configure,
            onConfigure = onConfigure,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
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
    onUpdateWidgetGridItem: (
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
            gridItem = gridItem,
            pinItemRequest = pinItemRequest,
            appWidgetId = appWidgetId,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
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
    gridItem: GridItem,
    pinItemRequest: PinItemRequest?,
    appWidgetId: Int,
    onUpdateWidgetGridItem: (
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
        onUpdateWidgetGridItem(gridItem.id, appWidgetId)
    } else {
        onDeleteWidgetGridItem(gridItem.id)
    }
}

private fun configureComponent(
    id: Int,
    appWidgetId: Int,
    configure: String?,
    onConfigure: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
) {
    val configureComponent = configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onUpdateWidgetGridItem(id, appWidgetId)
    }
}

private fun handleConfigureLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItem: GridItem?,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onDragEnd: (Int) -> Unit,
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
        onUpdateWidgetGridItem(gridItem.id, appWidgetId)
    } else {
        onDeleteWidgetGridItem(gridItem.id)
    }

    onDragEnd(targetPage)
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
    onConfigure: (Intent) -> Unit,
    onUpdateWidgetGridItem: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    val data = (gridItem?.data as? GridItemData.Widget) ?: return

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    if (result.resultCode == Activity.RESULT_OK) {
        val appWidgetId =
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        when (type) {
            GridItemSource.Type.New -> {
                configureComponent(
                    id = gridItem.id,
                    appWidgetId = appWidgetId,
                    configure = data.configure,
                    onConfigure = onConfigure,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                )
            }

            GridItemSource.Type.Pin -> {
                handleDragEndPinWidget(
                    gridItem = gridItem,
                    pinItemRequest = pinItemRequest,
                    appWidgetId = appWidgetId,
                    onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                )
            }

            null -> Unit
        }
    } else {
        onDeleteWidgetGridItem(gridItem.id)
    }

    onDragEnd(targetPage)
}