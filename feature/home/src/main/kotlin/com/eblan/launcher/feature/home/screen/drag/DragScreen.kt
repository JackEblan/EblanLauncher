package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.calculateTargetPage
import kotlin.math.roundToInt

@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    currentPage: Int,
    userData: UserData,
    dragOffset: Offset,
    lastGridItemLayoutInfo: GridItemLayoutInfo?,
    addGridItemLayoutInfo: GridItemLayoutInfo?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    overlaySize: IntSize,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = userData.infiniteScroll,
        pageCount = userData.pageCount,
    )

    var index by remember { mutableIntStateOf(page) }

    var newPage by remember { mutableStateOf(false) }

    var canScroll by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                if (index == 0 &&
                    userData.infiniteScroll &&
                    newPage &&
                    drag == Drag.Dragging
                ) {
                    index = userData.pageCount - 1
                } else if (index == 0 &&
                    userData.infiniteScroll &&
                    drag == Drag.Dragging
                ) {
                    newPage = true
                } else if (index > 0 &&
                    canScroll &&
                    drag == Drag.Dragging
                ) {
                    index -= 1
                }
            }

            PageDirection.Right -> {
                if (index == userData.pageCount - 1 &&
                    userData.infiniteScroll &&
                    newPage &&
                    drag == Drag.Dragging
                ) {
                    index = 0
                } else if (index == userData.pageCount - 1 &&
                    userData.infiniteScroll &&
                    drag == Drag.Dragging
                ) {
                    newPage = true
                } else if (index < userData.pageCount - 1 &&
                    canScroll &&
                    drag == Drag.Dragging
                ) {
                    index += 1
                }
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = newPage) {
        if (newPage) {
            canScroll = false

            onUpdatePageCount(userData.pageCount + 1)
        }
    }

    LaunchedEffect(key1 = userData.pageCount) {
        if (newPage) {
            index = userData.pageCount - 1

            canScroll = true
        }
    }

    LaunchedEffect(key1 = dragOffset) {
        if (lastGridItemLayoutInfo != null) {
            onMoveGridItem(
                index,
                lastGridItemLayoutInfo.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                lastGridItemLayoutInfo.screenWidth,
                lastGridItemLayoutInfo.screenHeight,
            )
        }

        if (addGridItemLayoutInfo != null) {
            onMoveGridItem(
                index,
                addGridItemLayoutInfo.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                addGridItemLayoutInfo.screenWidth,
                addGridItemLayoutInfo.screenHeight,
            )
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.End || drag == Drag.Cancel) {
            val targetPage = calculateTargetPage(
                currentPage = currentPage,
                index = index,
                infiniteScroll = userData.infiniteScroll,
                pageCount = userData.pageCount,
            )

            onDragEnd(targetPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray),
    ) {
        GridItemOverlay(
            modifier = Modifier.zIndex(1f),
            overlaySize = overlaySize,
            dragOffset = dragOffset.round(),
        )

        AnimatedContent(
            targetState = index,
            transitionSpec = {
                if (pageDirection == PageDirection.Right) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
                }.using(
                    SizeTransform(clip = false),
                )
            },
        ) { targetCount ->
            DragGridSubcomposeLayout(
                modifier = Modifier.fillMaxSize(),
                index = targetCount,
                rows = userData.rows,
                columns = userData.columns,
                gridItems = gridItems,
                gridItemContent = { gridItem ->
                    when (val gridItemData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(gridItemData = gridItemData)
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(gridItemData = gridItemData)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    overlaySize: IntSize,
    dragOffset: IntOffset,
) {
    val density = LocalDensity.current

    val width = with(density) {
        overlaySize.width.toDp()
    }

    val height = with(density) {
        overlaySize.height.toDp()
    }

    val size by remember {
        derivedStateOf {
            DpSize(width = width, height = height)
        }
    }

    Box(
        modifier = modifier
            .offset {
                dragOffset
            }
            .size(size)
            .background(Color.Green),
    ) {
        Text(text = "Drag")
    }
}