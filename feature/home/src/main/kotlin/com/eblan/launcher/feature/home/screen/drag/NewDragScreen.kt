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
import com.eblan.launcher.feature.home.component.grid.NewShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.NewWidgetGridItem
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
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDragEndPinShortcut: (
        targetPage: Int,
        id: Int,
        shortcutId: String,
        byteArray: ByteArray?,
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
            gridItemSource = gridItemSource,
            onUpdatePinWidget = onUpdatePinWidget,
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
            gridItemSource = gridItemSource,
            pinItemRequest = pinItemRequestWrapper.getPinItemRequest(),
            onDragEnd = onDragEnd,
            onConfigure = configureLauncher::launch,
            onUpdatePinWidget = onUpdatePinWidget,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        val gridItem = when (gridItemSource) {
            is GridItemSource.New -> {
                gridItemSource.gridItem
            }

            is GridItemSource.Pin -> {
                gridItemSource.gridItem
            }

            null -> null
        }

        handleDragIntOffset(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            gridItems = gridItems,
            dockGridItems = dockGridItems,
            drag = drag,
            gridItem = gridItem,
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
            onUpdatePinWidget = onUpdatePinWidget,
            onDragEndPinShortcut = onDragEndPinShortcut,
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
                                    NewWidgetGridItem(
                                        modifier = gridItemModifier,
                                        gridItemSource = gridItemSource,
                                        data = data,
                                    )
                                }

                                is GridItemData.ShortcutInfo -> {
                                    NewShortcutInfoGridItem(
                                        modifier = gridItemModifier,
                                        gridItemSource = gridItemSource,
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
                                NewWidgetGridItem(
                                    modifier = gridItemModifier,
                                    gridItemSource = gridItemSource,
                                    data = data,
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                NewShortcutInfoGridItem(
                                    modifier = gridItemModifier,
                                    gridItemSource = gridItemSource,
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
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDragEndPinShortcut: (
        targetPage: Int,
        id: Int,
        shortcutId: String,
        byteArray: ByteArray?,
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
                gridItemSource = gridItemSource,
                pinItemRequest = pinItemRequest,
                onConfigure = onConfigure,
                onDeleteGridItem = onDeleteGridItem,
                onUpdatePinWidget = onUpdatePinWidget,
                onDragEndPinShortcut = onDragEndPinShortcut,
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
    gridItemSource: GridItemSource?,
    pinItemRequest: PinItemRequest?,
    onConfigure: (Intent) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDragEndPinShortcut: (
        targetPage: Int,
        id: Int,
        shortcutId: String,
        byteArray: ByteArray?,
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
                        targetPage = targetPage,
                        pinItemRequest = pinItemRequest,
                        id = gridItemSource.gridItem.id,
                        shortcutId = data.id,
                        byteArray = gridItemSource.byteArray,
                        onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                        onDragEndPinShortcut = onDragEndPinShortcut,
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
                        pinItemRequest = pinItemRequest,
                        onUpdatePinWidget = onUpdatePinWidget,
                        onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                        onLaunch = onLaunch,
                        onDragEnd = onDragEnd,
                    )
                }

                else -> Unit
            }

        }

        null -> {
            onDragEnd(targetPage)
        }
    }
}

private fun onDragEndPinShortcut(
    targetPage: Int,
    pinItemRequest: PinItemRequest?,
    id: Int,
    shortcutId: String,
    byteArray: ByteArray?,
    onDeleteWidgetGridItem: (Int) -> Unit,
    onDragEndPinShortcut: (
        targetPage: Int,
        id: Int,
        shortcutId: String,
        byteArray: ByteArray?,
    ) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        pinItemRequest != null &&
        pinItemRequest.isValid &&
        pinItemRequest.accept()
    ) {
        onDragEndPinShortcut(
            targetPage,
            id,
            shortcutId,
            byteArray,
        )
    } else {
        onDeleteWidgetGridItem(id)
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

private fun handleConfigureLauncherResult(
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

private fun handleAppWidgetLauncherResult(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    result: ActivityResult,
    gridItemSource: GridItemSource?,
    pinItemRequest: PinItemRequest?,
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
                    pinItemRequest = pinItemRequest,
                    appWidgetId = appWidgetId,
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                )
            } else {
                onDeleteWidgetGridItem(gridItemSource.gridItem.id)
            }

            onDragEnd(targetPage)
        }

        null -> Unit
    }
}