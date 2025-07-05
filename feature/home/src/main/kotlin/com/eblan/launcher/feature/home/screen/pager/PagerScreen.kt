package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.widget.FrameLayout
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuPositionProvider
import com.eblan.launcher.feature.home.component.SettingsMenu
import com.eblan.launcher.feature.home.component.SettingsMenuPositionProvider
import com.eblan.launcher.feature.home.component.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.EblanApplicationComponentUiState
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.shortcut.ShortcutScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.detectTapGesturesUnConsume
import com.eblan.launcher.framework.widgetmanager.clearPressed
import kotlinx.coroutines.launch

@Composable
fun BoxScope.PagerScreen(
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
    onResetGridItemSource: (Int) -> Unit,
    onLongPressedGridItem: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onLongPressApplicationInfo: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onDraggingApplicationInfo: () -> Unit,
    onDragEndApplicationInfo: () -> Unit,
    onLongPressWidget: (
        currentPage: Int,
        imageBitmap: ImageBitmap?,
        intOffset: IntOffset,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragStartWidget: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onShowGridCache: (Screen) -> Unit,
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

    VerticalPager(
        state = verticalPagerState,
        modifier = modifier,
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
                    rootHeight = rootHeight,
                    drag = drag,
                    dragIntOffset = dragIntOffset,
                    verticalPagerIsScrollInProgress = verticalPagerState.isScrollInProgress,
                    onResetGridItemSource = onResetGridItemSource,
                    onLongPressedGridItem = onLongPressedGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onStartMainActivity = onStartMainActivity,
                    onEdit = onEdit,
                    onResize = onResize,
                    onSettings = onSettings,
                    onShowGridCache = onShowGridCache,
                )
            }

            1 -> {
                when (eblanApplicationComponentUiState) {
                    EblanApplicationComponentUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                                            isScrollInProgress = verticalPagerState.isScrollInProgress,
                                            appDrawerRowsHeight = appDrawerRowsHeight,
                                            onLongPressApplicationInfo = onLongPressApplicationInfo,
                                            onDragging = onDraggingApplicationInfo,
                                            onDragEnd = onDragEndApplicationInfo,
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
    onResetGridItemSource: (Int) -> Unit,
    rootHeight: Int,
    drag: Drag,
    dragIntOffset: IntOffset,
    verticalPagerIsScrollInProgress: Boolean,
    onLongPressedGridItem: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onStartMainActivity: (String?) -> Unit,
    onEdit: () -> Unit,
    onResize: (Int) -> Unit,
    onSettings: () -> Unit,
    onShowGridCache: (Screen) -> Unit,
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
                    onLongPress = {
                        showPopupSettingsMenu = true

                        onResetGridItemSource(
                            calculatePage(
                                index = horizontalPagerState.currentPage,
                                infiniteScroll = infiniteScroll,
                                pageCount = pageCount,
                            ),
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

            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                rows = rows,
                columns = columns,
                gridItems = gridItems[page],
                gridItemContent = { gridItem, x, y, width, height ->
                    when (val data = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                textColor = textColor,
                                gridItemData = data,
                                onTap = {
                                    onStartMainActivity(data.componentName)
                                },
                                onLongPress = { preview ->
                                    showPopupGridItemMenu = true

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
                                    )
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                drag = drag,
                                gridItemData = data,
                                onLongPress = { preview ->
                                    showPopupGridItemMenu = true

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
                                    )
                                },
                            )
                        }
                    }
                },
            )
        }

        GridSubcomposeLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            gridItems = dockGridItems,
        ) { gridItem, x, y, width, height ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        textColor = textColor,
                        gridItemData = data,
                        onTap = {
                            onStartMainActivity(data.componentName)
                        },
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                calculatePage(
                                    index = horizontalPagerState.currentPage,
                                    infiniteScroll = infiniteScroll,
                                    pageCount = pageCount,
                                ),
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = gridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y + (rootHeight - dockHeight),
                                ),
                            )
                        },
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(
                        drag = drag,
                        gridItemData = data,
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                calculatePage(
                                    index = horizontalPagerState.currentPage,
                                    infiniteScroll = infiniteScroll,
                                    pageCount = pageCount,
                                ),
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = gridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y + (rootHeight - dockHeight),
                                ),
                            )
                        },
                    )
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
            onShowGridCache = onShowGridCache,
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
    onLongPress: (ImageBitmap) -> Unit,
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
                    onLongPress = {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
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
    onLongPress: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.createView(
                    appWidgetId = gridItemData.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        gridItemData.width,
                        gridItemData.height,
                    )

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
                .pointerInput(Unit) {
                    detectTapGesturesUnConsume(
                        requireUnconsumed = false,
                        onLongPress = {
                            scope.launch {
                                onLongPress(graphicsLayer.toImageBitmap())
                            }
                        },
                    )
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
    onShowGridCache: (Screen) -> Unit,
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

                onShowGridCache(Screen.EditPage)
            },
        )
    }
}