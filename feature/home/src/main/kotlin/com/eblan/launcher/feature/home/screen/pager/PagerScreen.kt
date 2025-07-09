package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.menu.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.SettingsMenu
import com.eblan.launcher.feature.home.component.menu.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.menu.WidgetGridItemMenu
import com.eblan.launcher.feature.home.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.loading.LoadingScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.canScroll
import com.eblan.launcher.framework.widgetmanager.clearPressed
import kotlinx.coroutines.launch

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    targetPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dockHeight: Int,
    drag: Drag,
    dragIntOffset: IntOffset,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    rootWidth: Int,
    rootHeight: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    overlayIntOffset: IntOffset,
    initialPage: Int,
    onLongPressGrid: (
        currentPage: Int,
        intOffset: IntOffset,
    ) -> Unit,
    onLongPressedGridItem: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
        intOffset: IntOffset,
    ) -> Unit,
    onLongPressApplicationInfo: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
        dragIntOffset: IntOffset,
        overlayIntOffset: IntOffset,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDraggingApplicationInfo: () -> Unit,
    onLongPressWidget: (
        currentPage: Int,
        imageBitmap: ImageBitmap?,
        gridItemLayoutInfo: GridItemLayoutInfo,
        dragIntOffset: IntOffset,
        overlayIntOffset: IntOffset,
    ) -> Unit,
    onDragStartWidget: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
) {
    val gridHorizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + targetPage else targetPage,
        pageCount = {
            if (infiniteScroll) {
                Int.MAX_VALUE
            } else {
                pageCount
            }
        },
    )

    val verticalPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = {
            2
        },
    )

    var userScrollEnabled by remember { mutableStateOf(true) }

    VerticalPager(
        state = verticalPagerState,
        modifier = modifier,
        userScrollEnabled = userScrollEnabled,
    ) { verticalPage ->
        when (verticalPage) {
            0 -> {
                HorizontalPagerScreen(
                    horizontalPagerState = gridHorizontalPagerState,
                    rows = rows,
                    columns = columns,
                    pageCount = pageCount,
                    infiniteScroll = infiniteScroll,
                    dockRows = dockRows,
                    dockColumns = dockColumns,
                    gridItems = gridItems,
                    gridItemLayoutInfo = gridItemLayoutInfo,
                    dockHeight = dockHeight,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    onLongPressGrid = onLongPressGrid,
                    onLongPressedGridItem = onLongPressedGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onStartMainActivity = onStartMainActivity,
                    onEdit = onEdit,
                    onResize = onResize,
                    onSettings = onSettings,
                    onEditPage = onEditPage,
                    onUserScrollEnabled = { newUserScrollEnabled ->
                        userScrollEnabled = newUserScrollEnabled
                    },
                )
            }

            1 -> {
                when (eblanApplicationComponentUiState) {
                    EblanApplicationComponentUiState.Loading -> {
                        LoadingScreen()
                    }

                    is EblanApplicationComponentUiState.Success -> {
                        val applicationHorizontalPagerState = rememberPagerState(
                            initialPage = 0,
                            pageCount = {
                                eblanApplicationComponentUiState.eblanApplicationComponent.pageCount
                            },
                        )

                        Surface(modifier = Modifier.fillMaxSize()) {
                            HorizontalPager(state = applicationHorizontalPagerState) { page ->
                                when (page) {
                                    0 -> {
                                        ApplicationScreen(
                                            currentPage = gridHorizontalPagerState.currentPage,
                                            rows = rows,
                                            columns = columns,
                                            appDrawerColumns = appDrawerColumns,
                                            pageCount = pageCount,
                                            infiniteScroll = infiniteScroll,
                                            eblanApplicationInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanApplicationInfos,
                                            rootWidth = rootWidth,
                                            rootHeight = rootHeight,
                                            dockHeight = dockHeight,
                                            drag = drag,
                                            appDrawerRowsHeight = appDrawerRowsHeight,
                                            gridItemLayoutInfo = gridItemLayoutInfo,
                                            overlayIntOffset = overlayIntOffset,
                                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                                            onDragging = onDraggingApplicationInfo,
                                        )
                                    }

                                    1 -> {
                                        WidgetScreen(
                                            currentPage = gridHorizontalPagerState.currentPage,
                                            rows = rows,
                                            columns = columns,
                                            pageCount = pageCount,
                                            infiniteScroll = infiniteScroll,
                                            eblanAppWidgetProviderInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanAppWidgetProviderInfos,
                                            rootWidth = rootWidth,
                                            rootHeight = rootHeight,
                                            dockHeight = dockHeight,
                                            drag = drag,
                                            gridItemLayoutInfo = gridItemLayoutInfo,
                                            onLongPressWidget = onLongPressWidget,
                                            onDragStart = onDragStartWidget,
                                        )
                                    }

                                    2 -> {
                                        ShortcutScreen(eblanShortcutInfos = eblanApplicationComponentUiState.eblanApplicationComponent.eblanShortcutInfos)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalPagerScreen(
    modifier: Modifier = Modifier,
    horizontalPagerState: PagerState,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onLongPressGrid: (
        currentPage: Int,
        intOffset: IntOffset,
    ) -> Unit,
    rootWidth: Int,
    rootHeight: Int,
    drag: Drag,
    dragIntOffset: IntOffset,
    onLongPressedGridItem: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
        intOffset: IntOffset,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onUserScrollEnabled: (Boolean) -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var showPopupGridItemMenu by remember { mutableStateOf(false) }

    var showPopupSettingsMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Dragging && gridItemLayoutInfo != null) {
            showPopupGridItemMenu = false

            onDraggingGridItem()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onLongPress = { offset ->
                        showPopupSettingsMenu = true

                        onLongPressGrid(
                            calculatePage(
                                index = horizontalPagerState.currentPage,
                                infiniteScroll = infiniteScroll,
                                pageCount = pageCount,
                            ),
                            offset.round(),
                        )
                    },
                )
            }
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { index ->
            val page = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            GridLayout(
                modifier = Modifier.fillMaxSize(),
                rows = rows,
                columns = columns,
            ) {
                gridItems[page]?.forEach { gridItem ->
                    key(gridItem.id) {
                        val cellWidth = rootWidth / columns

                        val cellHeight = (rootHeight - dockHeight) / rows

                        val x = gridItem.startColumn * cellWidth

                        val y = gridItem.startRow * cellHeight

                        val width = gridItem.columnSpan * cellWidth

                        val height = gridItem.rowSpan * cellHeight

                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItem(
                                    modifier = Modifier.gridItem(gridItem),
                                    textColor = textColor,
                                    gridItemData = data,
                                    onTap = {
                                        onStartMainActivity(data.componentName)
                                    },
                                    onLongPress = { preview, intOffset ->
                                        showPopupGridItemMenu = true

                                        val dragX = x + intOffset.x

                                        val dragY = y + intOffset.y

                                        onLongPressedGridItem(
                                            page,
                                            preview,
                                            GridItemLayoutInfo(
                                                gridItem = gridItem,
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            ),
                                            IntOffset(x = dragX, y = dragY),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItem(
                                    modifier = Modifier.gridItem(gridItem),
                                    drag = drag,
                                    gridItemData = data,
                                    onLongPress = { preview, intOffset ->
                                        showPopupGridItemMenu = true

                                        val dragX = x + intOffset.x

                                        val dragY = y + intOffset.y

                                        onLongPressedGridItem(
                                            page,
                                            preview,
                                            GridItemLayoutInfo(
                                                gridItem = gridItem,
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            ),
                                            IntOffset(x = dragX, y = dragY),
                                        )
                                    },
                                    onUserScrollEnabled = onUserScrollEnabled,
                                )
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
                    val cellWidth = rootWidth / dockColumns

                    val cellHeight = dockHeight / dockRows

                    val x = dockGridItem.startColumn * cellWidth

                    val y = dockGridItem.startRow * cellHeight

                    val width = dockGridItem.columnSpan * cellWidth

                    val height = dockGridItem.rowSpan * cellHeight

                    when (val data = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                modifier = Modifier.gridItem(dockGridItem),
                                textColor = textColor,
                                gridItemData = data,
                                onTap = {
                                    onStartMainActivity(data.componentName)
                                },
                                onLongPress = { preview, intOffset ->
                                    showPopupGridItemMenu = true

                                    val dragX = x + intOffset.x

                                    val dragY = (rootHeight - dockHeight) + (y + intOffset.y)

                                    onLongPressedGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        preview,
                                        GridItemLayoutInfo(
                                            gridItem = dockGridItem,
                                            width = width,
                                            height = height,
                                            x = x,
                                            y = y + (rootHeight - dockHeight),
                                        ),
                                        IntOffset(x = dragX, y = dragY),
                                    )
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                modifier = Modifier.gridItem(dockGridItem),
                                drag = drag,
                                gridItemData = data,
                                onLongPress = { preview, intOffset ->
                                    showPopupGridItemMenu = true

                                    val dragX = x + intOffset.x

                                    val dragY = (rootHeight - dockHeight) + (y + intOffset.y)

                                    onLongPressedGridItem(
                                        calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        ),
                                        preview,
                                        GridItemLayoutInfo(
                                            gridItem = dockGridItem,
                                            width = width,
                                            height = height,
                                            x = x,
                                            y = y + (rootHeight - dockHeight),
                                        ),
                                        IntOffset(x = dragX, y = dragY),
                                    )
                                },
                                onUserScrollEnabled = onUserScrollEnabled,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPopupGridItemMenu && gridItemLayoutInfo?.gridItem != null) {
        when (gridItemLayoutInfo.gridItem.associate) {
            Associate.Grid -> {
                PopupGridItemMenu(
                    x = gridItemLayoutInfo.x,
                    y = gridItemLayoutInfo.y,
                    width = gridItemLayoutInfo.width,
                    height = gridItemLayoutInfo.height,
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                val showResize =
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                WidgetGridItemMenu(
                                    showResize = showResize,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            }

            Associate.Dock -> {
                PopupGridItemMenu(
                    x = gridItemLayoutInfo.x,
                    y = rootHeight - dockHeight,
                    width = gridItemLayoutInfo.width,
                    height = gridItemLayoutInfo.height,
                    onDismissRequest = {
                        showPopupGridItemMenu = false
                    },
                    content = {
                        when (val data = gridItemLayoutInfo.gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItemMenu(
                                    showResize = gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }

                            is GridItemData.Widget -> {
                                val showResize =
                                    gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                WidgetGridItemMenu(
                                    showResize = showResize,
                                    onEdit = onEdit,
                                    onResize = {
                                        onResize(
                                            calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = pageCount,
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    if (showPopupSettingsMenu) {
        PopupSettingsMenu(
            dragIntOffset = dragIntOffset,
            onSettings = onSettings,
            onEditPage = onEditPage,
            onDismissRequest = {
                showPopupSettingsMenu = false
            },
        )
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    textColor: TextColor,
    gridItemData: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: (
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
    ) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGesturesUnConsume(
                    onTap = {
                        onTap()
                    },
                    onLongPress = { offset ->
                        scope.launch {
                            onLongPress(
                                graphicsLayer.toImageBitmap(),
                                offset.round(),
                            )
                        }
                    },
                )
            }
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AsyncImage(
            model = gridItemData.icon,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp, 40.dp)
                .weight(1f),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = gridItemData.label.toString(),
            modifier = Modifier.weight(1f),
            color = color,
            textAlign = TextAlign.Center,
            fontSize = TextUnit(
                value = 10f,
                type = TextUnitType.Sp,
            ),
        )
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    drag: Drag,
    gridItemData: GridItemData.Widget,
    onLongPress: (
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
    ) -> Unit,
    onUserScrollEnabled: (Boolean) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    var canScroll by remember { mutableStateOf(false) }

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = gridItemData.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                    )

                    canScroll = canScroll(this)

                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            },
            modifier = modifier
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }

                    drawLayer(graphicsLayer)
                }
                .run {
                    if (canScroll) {
                        pointerInput(Unit) {
                            detectTapGesturesUnConsume(
                                requireUnconsumed = false,
                                onLongPress = { offset ->
                                    scope.launch {
                                        onLongPress(
                                            graphicsLayer.toImageBitmap(),
                                            offset.round(),
                                        )
                                    }
                                },
                                onPress = {
                                    onUserScrollEnabled(false)

                                    try {
                                        awaitRelease()

                                        onUserScrollEnabled(true)
                                    } catch (e: GestureCancellationException) {
                                        onUserScrollEnabled(true)
                                    }
                                },
                            )
                        }
                    } else {
                        pointerInput(Unit) {
                            detectTapGesturesUnConsume(
                                requireUnconsumed = false,
                                onLongPress = { offset ->
                                    scope.launch {
                                        onLongPress(
                                            graphicsLayer.toImageBitmap(),
                                            offset.round(),
                                        )
                                    }
                                },
                            )
                        }
                    }
                },
            update = { appWidgetHostView ->
                if (drag == Drag.Start) {
                    appWidgetHostView.clearPressed(view = appWidgetHostView)
                }
            },
        )
    }
}

@Composable
fun PopupGridItemMenu(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
        ),
        onDismissRequest = onDismissRequest,
        content = content,
    )
}

@Composable
private fun PopupSettingsMenu(
    modifier: Modifier = Modifier,
    dragIntOffset: IntOffset,
    onSettings: () -> Unit,
    onEditPage: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Popup(
        popupPositionProvider = SettingsMenuPositionProvider(
            x = dragIntOffset.x,
            y = dragIntOffset.y,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        SettingsMenu(
            modifier = modifier,
            onSettings = {
                onDismissRequest()

                onSettings()
            },
            onEditPage = {
                onDismissRequest()

                onEditPage()
            },
        )
    }
}