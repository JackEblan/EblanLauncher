package com.eblan.launcher.feature.home.screen.drag

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
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.delay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    gridItem: GridItem?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
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
) {
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

    LaunchedEffect(key1 = dragIntOffset) {
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
        when (drag) {
            Drag.End -> {
                val targetPage = calculatePage(
                    index = currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = pageCount,
                )

                onDragEnd(targetPage)
            }

            Drag.Cancel -> {
                onDragCancel()
            }

            else -> Unit
        }
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
                                    WidgetGridItem(
                                        modifier = gridItemModifier,
                                        data = data,
                                    )
                                }

                                is GridItemData.ShortcutInfo -> {
                                    ShortcutInfoGridItem(
                                        modifier = gridItemModifier,
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
                                WidgetGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                ShortcutInfoGridItem(
                                    modifier = gridItemModifier,
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

suspend fun handlePageDirection(
    currentPage: Int,
    pageDirection: PageDirection?,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    when (pageDirection) {
        PageDirection.Left -> {
            onAnimateScrollToPage(currentPage - 1)
        }

        PageDirection.Right -> {
            onAnimateScrollToPage(currentPage + 1)
        }

        null -> Unit
    }
}

suspend fun handleDragIntOffset(
    currentPage: Int,
    infiniteScroll: Boolean,
    pageCount: Int,
    gridItems: Map<Int, List<GridItem>>,
    dockGridItems: List<GridItem>,
    drag: Drag,
    gridItem: GridItem?,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    dockHeight: Int,
    gridPadding: Int,
    rootWidth: Int,
    dockColumns: Int,
    dockRows: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    onChangePageDirection: (PageDirection) -> Unit,
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
) {
    if (drag != Drag.Dragging ||
        gridItem == null ||
        isScrollInProgress
    ) {
        return
    }

    val targetPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val gridItemsByPage = gridItems[targetPage].orEmpty()

    val isDraggingOnDock =
        dragIntOffset.y > (rootHeight - dockHeight) - gridPadding

    val delay = 500L

    if (dragIntOffset.x <= gridPadding && !isDraggingOnDock) {
        delay(delay)

        onChangePageDirection(PageDirection.Left)
    } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
        delay(delay)

        onChangePageDirection(PageDirection.Right)
    } else if (isDraggingOnDock) {
        delay(delay)

        val cellWidth = rootWidth / dockColumns

        val cellHeight = dockHeight / dockRows

        val dockY = dragIntOffset.y - (rootHeight - dockHeight)

        val newGridItem = gridItem.copy(
            page = targetPage,
            startRow = dockY / cellHeight,
            startColumn = dragIntOffset.x / cellWidth,
            associate = Associate.Dock,
        )

        if (isGridItemSpanWithinBounds(
                gridItem = newGridItem,
                rows = dockRows,
                columns = dockColumns,
            )
        ) {
            onMoveGridItem(
                gridItemsByPage + dockGridItems,
                newGridItem,
                dragIntOffset.x,
                dockY,
                dockRows,
                dockColumns,
                rootWidth,
                dockHeight,
            )
        }
    } else {
        delay(delay)

        val gridWidth = rootWidth - (gridPadding * 2)

        val gridHeight = (rootHeight - dockHeight) - (gridPadding * 2)

        val gridX = dragIntOffset.x - gridPadding

        val gridY = dragIntOffset.y - gridPadding

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val newGridItem = gridItem.copy(
            page = targetPage,
            startRow = gridY / cellHeight,
            startColumn = gridX / cellWidth,
            associate = Associate.Grid,
        )

        if (isGridItemSpanWithinBounds(
                gridItem = newGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            onMoveGridItem(
                gridItemsByPage + dockGridItems,
                newGridItem,
                gridX,
                gridY,
                rows,
                columns,
                gridWidth,
                gridHeight,
            )
        }
    }
}