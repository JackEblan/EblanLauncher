package com.eblan.launcher.feature.home.screen.editpage

import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
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
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.Grid
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.gestures.detectTapGesturesUnConsume
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.screen.drag.handlePageDirection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EditPageScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    textColor: TextColor,
    gridItems: Map<Int, List<GridItem>>,
    dragIntOffset: IntOffset,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    movedCurrentPage: Int?,
    onSaveEditPage: (Int) -> Unit,
    onCancelEditPage: (Int) -> Unit,
    onLongPress: (
        imageBitmap: ImageBitmap,
        dragIntOffset: IntOffset,
        overlayIntOffset: IntOffset,
    ) -> Unit,
    onMovePage: (from: Int, to: Int) -> Unit,
    onDragEnd: () -> Unit,
    onResetMovedPages: () -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val density = LocalDensity.current

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val horizontalPagerState = rememberPagerState(
        initialPage = currentPage,
        pageCount = {
            pageCount
        },
    )

    val horizontalPagerPadding = 20.dp

    val gridPadding = 5.dp

    val horizontalPagerPaddingPx = with(density) {
        horizontalPagerPadding.roundToPx()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var animatedContentPageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            selectedIndex = selectedIndex,
            currentPage = horizontalPagerState.currentPage,
            movedCurrentPage = movedCurrentPage,
            drag = drag,
            dragIntOffset = dragIntOffset,
            horizontalPagerPaddingPx = horizontalPagerPaddingPx,
            rootWidth = rootWidth,
            isScrollInProgress = horizontalPagerState.isScrollInProgress,
            onChangePageDirection = { newPageDirection ->
                animatedContentPageDirection = newPageDirection

                pageDirection = newPageDirection
            },
            onMovePage = onMovePage,
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
        if (drag == Drag.Cancel || drag == Drag.End) {
            onDragEnd()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(all = horizontalPagerPadding),
        ) { index ->
            AnimatedContent(
                targetState = gridItems[index],
                transitionSpec = {
                    when (animatedContentPageDirection) {
                        PageDirection.Left -> {
                            fadeIn(
                                animationSpec = keyframes {
                                    0f at 0
                                    0f at 250
                                    1f at 500
                                    durationMillis = 500
                                },
                            ) togetherWith
                                    slideOutHorizontally(animationSpec = tween(durationMillis = 500)) { width -> width }
                        }

                        PageDirection.Right -> {
                            fadeIn(
                                animationSpec = keyframes {
                                    0f at 0
                                    0f at 250
                                    1f at 500
                                    durationMillis = 500
                                },
                            ) togetherWith
                                    slideOutHorizontally(animationSpec = tween(durationMillis = 500)) { width -> -width }
                        }

                        null -> {
                            fadeIn() togetherWith fadeOut()
                        }
                    }.using(
                        SizeTransform(clip = false),
                    )
                },
            ) { targetGridItems ->

                val graphicsLayer = rememberGraphicsLayer()

                Grid(
                    modifier = Modifier
                        .drawWithContent {
                            graphicsLayer.record {
                                this@drawWithContent.drawContent()
                            }

                            drawLayer(graphicsLayer)
                        }
                        .pointerInput(Unit) {
                            detectTapGesturesUnConsume(
                                onLongPress = { offset ->
                                    scope.launch {
                                        selectedIndex = index

                                        animatedContentPageDirection =
                                            if (index > horizontalPagerState.currentPage) {
                                                PageDirection.Left
                                            } else if (index < horizontalPagerState.currentPage) {
                                                PageDirection.Right
                                            } else {
                                                null
                                            }

                                        onResetMovedPages()

                                        val dragX = horizontalPagerPaddingPx + offset.round().x

                                        val dragY = horizontalPagerPaddingPx + offset.round().y

                                        onLongPress(
                                            graphicsLayer.toImageBitmap(),
                                            IntOffset(x = dragX, y = dragY),
                                            IntOffset(
                                                x = horizontalPagerPaddingPx,
                                                y = horizontalPagerPaddingPx,
                                            ),
                                        )
                                    }
                                },
                            )
                        }
                        .fillMaxSize()
                        .padding(gridPadding)
                        .border(
                            width = 2.dp,
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .background(color = Color.White.copy(alpha = 0.25f)),
                ) {
                    targetGridItems?.forEach { gridItem ->
                        key(gridItem.id) {
                            val gridWidth = rootWidth - (horizontalPagerPaddingPx * 2)

                            val gridHeight =
                                (rootHeight - dockHeight) - (horizontalPagerPaddingPx * 2)

                            val cellWidth = gridWidth / columns

                            val cellHeight = gridHeight / rows

                            val x = gridItem.startColumn * cellWidth

                            val y = gridItem.startRow * cellHeight

                            val width = gridItem.columnSpan * cellWidth

                            val height = gridItem.rowSpan * cellHeight

                            when (val gridItemData = gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    Column(
                                        modifier = Modifier
                                            .gridItem(
                                                width = width,
                                                height = height,
                                                x = x,
                                                y = y,
                                            )
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

                                is GridItemData.Widget -> {
                                    val appWidgetInfo =
                                        appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

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

                                                    setAppWidget(appWidgetId, appWidgetInfo)
                                                }
                                            },
                                            modifier = Modifier
                                                .gridItem(
                                                    width = width,
                                                    height = height,
                                                    x = x,
                                                    y = y,
                                                )
                                                .fillMaxSize(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Button(
                onClick = {
                    onCancelEditPage(horizontalPagerState.currentPage)
                },
            ) {
                Text(text = "Cancel")
            }

            Button(
                onClick = {
                    onSaveEditPage(horizontalPagerState.currentPage)
                },
            ) {
                Text(text = "Save")
            }
        }
    }
}

private suspend fun handleDragIntOffset(
    selectedIndex: Int,
    currentPage: Int,
    movedCurrentPage: Int?,
    drag: Drag,
    dragIntOffset: IntOffset,
    horizontalPagerPaddingPx: Int,
    rootWidth: Int,
    isScrollInProgress: Boolean,
    onChangePageDirection: (PageDirection) -> Unit,
    onMovePage: (from: Int, to: Int) -> Unit,
) {
    if (drag == Drag.Dragging && !isScrollInProgress) {
        val scrollToPageDelay = 500L

        val moveGridItemDelay = 100L

        if (dragIntOffset.x <= horizontalPagerPaddingPx) {
            delay(scrollToPageDelay)

            onChangePageDirection(PageDirection.Left)
        } else if (dragIntOffset.x >= rootWidth - horizontalPagerPaddingPx) {
            delay(scrollToPageDelay)

            onChangePageDirection(PageDirection.Right)
        } else if (movedCurrentPage != null) {
            delay(moveGridItemDelay)

            onMovePage(movedCurrentPage, currentPage)
        } else {
            delay(moveGridItemDelay)

            onMovePage(selectedIndex, currentPage)
        }
    }
}