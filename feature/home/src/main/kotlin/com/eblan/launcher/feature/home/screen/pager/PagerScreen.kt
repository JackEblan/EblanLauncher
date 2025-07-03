package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.pressGridItem
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
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationComponentUiState: EblanApplicationComponentUiState,
    rootWidth: Int,
    rootHeight: Int,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    onLongPressGrid: (Int) -> Unit,
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
                    rootHeight = rootHeight,
                    onLongPressGrid = onLongPressGrid,
                    onLongPressedGridItem = onLongPressedGridItem,
                    onDraggingGridItem = onDraggingGridItem,
                    onEnableUserScroll = { newUserScrollEnabled ->
                        userScrollEnabled = newUserScrollEnabled
                    },
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
                                            appDrawerRowsHeight = appDrawerRowsHeight,
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
    onLongPressGrid: (Int) -> Unit,
    rootHeight: Int,
    onLongPressedGridItem: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDraggingGridItem: () -> Unit,
    onEnableUserScroll: (Boolean) -> Unit,
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

    var menuOffset by remember { mutableStateOf(IntOffset.Zero) }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { offset ->
                        val page = calculatePage(
                            index = horizontalPagerState.currentPage,
                            infiniteScroll = infiniteScroll,
                            pageCount = pageCount,
                        )

                        menuOffset = offset.round()

                        showPopupSettingsMenu = true

                        onLongPressGrid(page)
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
            val horizontalPage = calculatePage(
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            GridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                rows = rows,
                columns = columns,
                gridItems = gridItems[horizontalPage],
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
                                        horizontalPage,
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
                                onDragging = {
                                    showPopupGridItemMenu = false

                                    onDraggingGridItem()
                                },
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                gridItemData = data,
                                onLongPress = { preview ->
                                    showPopupGridItemMenu = true

                                    onLongPressedGridItem(
                                        horizontalPage,
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
                                onEnableUserScroll = onEnableUserScroll,
                                onDragging = {
                                    showPopupGridItemMenu = false

                                    onDraggingGridItem()
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
            val horizontalPage = calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

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
                                horizontalPage,
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
                        onDragging = {
                            showPopupGridItemMenu = false

                            onDraggingGridItem()
                        },
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(
                        gridItemData = data,
                        onLongPress = { preview ->
                            showPopupGridItemMenu = true

                            onLongPressedGridItem(
                                horizontalPage,
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
                        onEnableUserScroll = onEnableUserScroll,
                        onDragging = {
                            showPopupGridItemMenu = false

                            onDraggingGridItem()
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
                                        val horizontalPage = calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        )

                                        onResize(horizontalPage)
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
                                        val horizontalPage = calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        )

                                        onResize(horizontalPage)
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
                                        val horizontalPage = calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        )

                                        onResize(horizontalPage)
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
                                        val horizontalPage = calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        )

                                        onResize(horizontalPage)
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
            dragIntOffset = menuOffset,
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
    onDragging: () -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressGridItem(
                            longPressTimeoutMillis = viewConfiguration.longPressTimeoutMillis,
                            onTap = onTap,
                            onLongPress = {
                                onLongPress(graphicsLayer.toImageBitmap())
                            },
                            onDragging = onDragging,
                        )
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
    gridItemData: GridItemData.Widget,
    onLongPress: (ImageBitmap) -> Unit,
    onEnableUserScroll: (Boolean) -> Unit,
    onDragging: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.setOnTouchEventListener { event, canScrollVertically ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            if (canScrollVertically) {
                                onEnableUserScroll(false)
                            }
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE -> {
                            if (canScrollVertically) {
                                onEnableUserScroll(true)
                            }
                        }

                        MotionEvent.ACTION_CANCEL -> {
                            onDragging()
                        }
                    }
                }

                appWidgetHost.createView(
                    appWidgetId = gridItemData.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        gridItemData.width,
                        gridItemData.height,
                    )

                    setOnLongClickListener {
                        scope.launch {
                            onLongPress(graphicsLayer.toImageBitmap())
                        }

                        true
                    }

                    setAppWidget(appWidgetId, appWidgetInfo)
                }
            },
            modifier = modifier.drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
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