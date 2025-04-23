package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetProviderInfo
import android.widget.FrameLayout
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemBody
import com.eblan.launcher.feature.home.component.ApplicationInfoMenuOverlay
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItemBody
import com.eblan.launcher.feature.home.component.WidgetMenuOverlay
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.launch

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
    showBottomSheet: Boolean,
    userScrollEnabled: Boolean,
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
    LaunchedEffect(key1 = drag) {
        if (drag is Drag.Start && !showBottomSheet) {
            onDragStart()
        }
    }

    HorizontalPager(state = pagerState, userScrollEnabled = userScrollEnabled) { index ->
        val page = calculatePage(
            index = index,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
        )

        GridSubcomposeLayout(
            modifier = modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            onShowBottomSheet()
                        },
                    )
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
                            onTap = {
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
            menuContent = { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoMenuOverlay(
                            onEdit = onEdit,
                            onResize = onResize,
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetMenuOverlay(
                            showResize = data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE,
                            onEdit = onEdit,
                            onResize = onResize,
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    val scope = rememberCoroutineScope()

    ApplicationInfoGridItemBody(
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
        gridItemData = gridItemData,
    )
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

    val graphicsLayer = rememberGraphicsLayer()

    val currentOnLongPress by rememberUpdatedState(onLongPress)

    val scope = rememberCoroutineScope()

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

                setOnLongClickListener {
                    scope.launch {
                        currentOnLongPress(graphicsLayer.toImageBitmap())
                    }

                    true
                }

                setAppWidget(appWidgetId, appWidgetInfo)
            },
        )
    }
}