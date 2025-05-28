package com.eblan.launcher.feature.home.screen.pager

import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuPositionProvider
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.screen.application.ApplicationScreen
import com.eblan.launcher.feature.home.screen.widget.WidgetScreen
import com.eblan.launcher.feature.home.util.calculatePage
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
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfosByGroup: Map<EblanApplicationInfo, List<EblanAppWidgetProviderInfo>>,
    rootWidth: Int,
    rootHeight: Int,
    appDrawerColumns: Int,
    dragIntOffset: IntOffset,
    onLongPressGrid: (Int) -> Unit,
    onLongPressedGridItem: (
        currentPage: Int,
        addNewPage: Boolean,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onLongPressApplicationInfo: (
        currentPage: Int,
        imageBitmap: ImageBitmap,
        intOffset: IntOffset,
        size: IntSize,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragStart: () -> Unit,
    onDraggingApplicationInfo: () -> Unit,
    onDragEndApplicationInfo: () -> Unit,
    onLongPressWidget: (ImageBitmap?) -> Unit,
    onDragStartWidget: (intOffset: IntOffset, intSize: IntSize, GridItemLayoutInfo) -> Unit,
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
        pageCount = { 2 },
    )

    val applicationHorizontalPagerState = rememberPagerState(
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
                    drag = drag,
                    dockGridItems = dockGridItems,
                    textColor = textColor,
                    rootHeight = rootHeight,
                    onLongPressGrid = onLongPressGrid,
                    onLongPressedGridItem = onLongPressedGridItem,
                    onLaunchApplication = onLaunchApplication,
                    onDragStart = onDragStart,
                    onWidgetActionDown = {
                        userScrollEnabled = false
                    },
                    onWidgetActionUp = {
                        userScrollEnabled = true
                    },
                )
            }

            1 -> {
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
                                eblanApplicationInfos = eblanApplicationInfos,
                                rootWidth = rootWidth,
                                dockHeight = dockHeight,
                                drag = drag,
                                textColor = textColor,
                                rootHeight = rootHeight,
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
                                dragIntOffset = dragIntOffset,
                                eblanAppWidgetProviderInfosByGroup = eblanAppWidgetProviderInfosByGroup,
                                rootWidth = rootWidth,
                                rootHeight = rootHeight,
                                dockHeight = dockHeight,
                                drag = drag,
                                textColor = textColor,
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
    drag: Drag,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onLongPressGrid: (Int) -> Unit,
    rootHeight: Int,
    onLongPressedGridItem: (
        currentPage: Int,
        addNewPage: Boolean,
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onLaunchApplication: (String) -> Unit,
    onDragStart: () -> Unit,
    onWidgetActionDown: () -> Unit,
    onWidgetActionUp: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Start && gridItemLayoutInfo != null) {
            onDragStart()
        }
    }

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        val horizontalPage = calculatePage(
                            index = horizontalPagerState.currentPage,
                            infiniteScroll = infiniteScroll,
                            pageCount = pageCount,
                        )

                        onLongPressGrid(horizontalPage)
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
                page = horizontalPage,
                rows = rows,
                columns = columns,
                gridItems = gridItems,
                gridItemContent = { gridItem, x, y, width, height ->
                    when (val data = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                textColor = textColor,
                                gridItemData = data,
                                onTap = {
                                    onLaunchApplication(data.packageName)
                                },
                                onLongPress = { preview ->
                                    onLongPressedGridItem(
                                        horizontalPage,
                                        (gridItems[horizontalPage]?.size ?: 0) > 1,
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
                                gridItemData = data,
                                onLongPress = { preview ->
                                    onLongPressedGridItem(
                                        horizontalPage,
                                        (gridItems[horizontalPage]?.size ?: 0) > 1,
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
                                onWidgetActionDown = onWidgetActionDown,
                                onWidgetActionUp = onWidgetActionUp,
                            )
                        }
                    }
                },
            )
        }

        DockGrid(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            dockGridItems = dockGridItems,
        ) { dockGridItem, x, y, width, height ->
            val horizontalPage = calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            when (val data = dockGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        textColor = textColor,
                        gridItemData = data,
                        onTap = {
                            onLaunchApplication(data.packageName)
                        },
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                horizontalPage,
                                !gridItems[horizontalPage].isNullOrEmpty(),
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = dockGridItem,
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
                        gridItemData = data,
                        onLongPress = { preview ->
                            onLongPressedGridItem(
                                horizontalPage,
                                !gridItems[horizontalPage].isNullOrEmpty(),
                                preview,
                                GridItemLayoutInfo(
                                    gridItem = dockGridItem,
                                    width = width,
                                    height = height,
                                    x = x,
                                    y = y + (rootHeight - dockHeight),
                                ),
                            )
                        },
                        onWidgetActionDown = onWidgetActionDown,
                        onWidgetActionUp = onWidgetActionUp,
                    )
                }
            }
        }
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

    val scope = rememberCoroutineScope()

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
            text = gridItemData.label,
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
    onWidgetActionDown: () -> Unit,
    onWidgetActionUp: () -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                appWidgetHost.setOnTouchEventListener { event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onWidgetActionDown()
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_CANCEL -> {
                            onWidgetActionUp()
                        }
                    }
                }

                appWidgetHost.createView(
                    appWidgetId = gridItemData.appWidgetId,
                    appWidgetProviderInfo = appWidgetInfo,
                ).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
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
fun GridItemMenu(
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