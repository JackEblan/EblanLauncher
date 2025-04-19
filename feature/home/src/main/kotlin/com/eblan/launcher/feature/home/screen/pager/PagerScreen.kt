package com.eblan.launcher.feature.home.screen.pager

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.GridSubcomposeLayout
import com.eblan.launcher.feature.home.component.MenuOverlay
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun PagerScreen(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    userData: UserData,
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
            infiniteScroll = userData.infiniteScroll,
            pageCount = userData.pageCount,
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
            rows = userData.rows,
            columns = userData.columns,
            gridItemLayoutInfo = gridItemLayoutInfo,
            gridItems = gridItems,
            showMenu = showMenu,
            onDismissRequest = onDismissRequest,
            gridItemContent = { gridItem, x, y, width, height, screenWidth, screenHeight ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoGridItem(
                            gridItemData = gridItemData,
                            onTap = {
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

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            gridItemData = gridItemData,
                            onTap = {
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
    onTap: () -> Unit,
    onLongPress: (ImageBitmap) -> Unit,
) {
    val graphicsLayer = rememberGraphicsLayer()

    var isLongPress by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = isLongPress) {
        if (isLongPress) {
            onLongPress(graphicsLayer.toImageBitmap())

            isLongPress = false
        }
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
                awaitEachGesture {
                    val down = awaitFirstDown()

                    onTap()

                    val longPress = awaitLongPressOrCancellation(down.id)

                    if (longPress != null) {
                        isLongPress = true
                    }
                }
            }

            .fillMaxSize()
            .background(Color.Blue),
    ) {
        AsyncImage(model = gridItemData.icon, contentDescription = null)

        Text(text = gridItemData.label)
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
    onTap: () -> Unit,
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

                    setOnClickListener {
                        onTap()
                    }

                    setOnLongClickListener {
                        isLongPress = true
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