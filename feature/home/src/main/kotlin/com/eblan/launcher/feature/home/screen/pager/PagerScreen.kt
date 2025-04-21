package com.eblan.launcher.feature.home.screen.pager

import android.widget.FrameLayout
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastForEach
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemBody
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuOverlay
import com.eblan.launcher.feature.home.component.WidgetGridItemBody
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    showMenu: Boolean,
    drag: Drag,
    onDismissRequest: () -> Unit,
    onShowBottomSheet: () -> Unit,
    onLongPressedGridItem: (
        imageBitmap: ImageBitmap,
        gridItemLayoutInfo: GridItemLayoutInfo,
    ) -> Unit,
    onDragStart: () -> Unit,
    onLaunchApplication: (String) -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    var hitCounter by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = drag) {
        if (drag is Drag.Start) {
            if (hitCounter > 1) {
                onDragStart()
            } else {
                onShowBottomSheet()
            }
        }
    }

    HorizontalPager(state = pagerState) { index ->
        val page = calculatePage(
            index = index,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(pass = PointerEventPass.Initial)

                        hitCounter = 1
                    }
                }
                .fillMaxSize(),
            page = page,
            rows = rows,
            columns = columns,
            gridItemLayoutInfo = gridItemLayoutInfo,
            gridItems = gridItems,
            showMenu = showMenu,
            onDismissRequest = onDismissRequest,
            gridItemContent = { gridItem, x, y, width, height, screenWidth, screenHeight ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoGridItem(
                            gridItemData = data,
                            onFirstDown = {
                                hitCounter += 1
                            },
                            onPress = {
                                onLaunchApplication(data.packageName)
                            },
                            onLongPress = { preview ->
                                onLongPressedGridItem(
                                    preview,
                                    GridItemLayoutInfo(
                                        gridItem = gridItem,
                                        width = width,
                                        height = height,
                                        x = x,
                                        y = y,
                                        screenWidth = screenWidth,
                                        screenHeight = screenHeight,
                                    ),
                                )
                            },
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            gridItemData = data,
                            onFirstDown = {
                                hitCounter += 1
                            },
                            onLongPress = { preview ->
                                onLongPressedGridItem(
                                    preview,
                                    GridItemLayoutInfo(
                                        gridItem = gridItem,
                                        width = width,
                                        height = height,
                                        x = x,
                                        y = y,
                                        screenWidth = screenWidth,
                                        screenHeight = screenHeight,
                                    ),
                                )
                            },
                        )
                    }
                }
            },
            menuContent = {
                MenuOverlay(
                    onEdit = onEdit,
                    onResize = onResize,
                )
            },
        )
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
    onFirstDown: () -> Unit,
    onPress: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    var isLongPress by remember { mutableStateOf(false) }

    var isPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress) {
        if (isLongPress) {
            onLongPress(graphicsLayer.toImageBitmap())

            isLongPress = false
        }
    }

    LaunchedEffect(key1 = isPress) {
        if (isPress) {
            onPress()

            isPress = false
        }
    }


    ApplicationInfoGridItemBody(
        modifier = modifier
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()

                    onFirstDown()

                    val longPress = awaitLongPressOrCancellation(down.id)

                    if (longPress != null) {
                        isLongPress = true
                    } else {
                        currentEvent.changes.fastForEach {
                            isPress = it.changedToUp()
                        }
                    }
                }
            }
            .fillMaxSize(),
        gridItemData = gridItemData,
    )
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
    onFirstDown: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    var isLongPress by remember { mutableStateOf(false) }

    val graphicsLayer = rememberGraphicsLayer()

    LaunchedEffect(key1 = isLongPress) {
        if (isLongPress) {
            onLongPress(graphicsLayer.toImageBitmap())

            isLongPress = false
        }
    }

    if (appWidgetInfo != null) {
        WidgetGridItemBody(
            modifier = modifier.drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }

                drawLayer(graphicsLayer)
            },
            appWidgetHostView = appWidgetHost.createView(
                appWidgetId = gridItemData.appWidgetId,
                appWidgetProviderInfo = appWidgetInfo,
            ).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                )

                setOnClickListener {
                    onFirstDown()
                }

                setOnLongClickListener {
                    isLongPress = true
                    true
                }

                setAppWidget(appWidgetId, appWidgetInfo)
            },
        )
    }
}