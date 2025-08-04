package com.eblan.launcher.feature.home.screen.drag

import android.appwidget.AppWidgetManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.FolderGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    startCurrentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItemsByPage: Map<Int, List<GridItem>>,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: Long,
    movedGridItems: Boolean,
    updatedGridItem: GridItem?,
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
    onMoveGridItemsFailed: (Int) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
) {
    requireNotNull(gridItemSource)

    val appWidgetHostWrapper = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var lastAppWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    val horizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + startCurrentPage else startCurrentPage,
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

    val color = Color(textColor)

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            resultCode = result.resultCode,
            updatedGridItem = updatedGridItem,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDragEnd = onDragEnd,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            result = result,
            gridItem = gridItemSource.gridItem,
            onUpdateGridItemDataCache = onUpdateGridItemDataCache,
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            gridItemsByPage = gridItemsByPage,
            dockGridItems = dockGridItems,
            drag = drag,
            gridItem = gridItemSource.gridItem,
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
            onUpdatePageDirection = { newPageDirection ->
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
                handleOnDragEnd(
                    currentPage = horizontalPagerState.currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = pageCount,
                    movedGridItems = movedGridItems,
                    androidAppWidgetHostWrapper = appWidgetHostWrapper,
                    appWidgetManager = appWidgetManager,
                    gridItemSource = gridItemSource,
                    onLaunch = appWidgetLauncher::launch,
                    onDragEnd = onDragEnd,
                    onMoveGridItemsFailed = onMoveGridItemsFailed,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                )
            }

            Drag.Cancel -> {
                onDragCancel()
            }

            else -> Unit
        }
    }

    LaunchedEffect(key1 = deleteAppWidgetId) {
        handleDeleteAppWidgetId(
            gridItem = gridItemSource.gridItem,
            appWidgetId = lastAppWidgetId,
            deleteAppWidgetId = deleteAppWidgetId,
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDragEnd = onDragEnd,
        )
    }

    LaunchedEffect(key1 = updatedGridItem) {
        handleBoundWidget(
            gridItemSource = gridItemSource,
            updatedGridItem = updatedGridItem,
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            onConfigure = configureLauncher::launch,
            onDragEnd = onDragEnd,
            onDeleteGridItemCache = onDeleteGridItemCache,
        )
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
                    .background(
                        color = color.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = color,
                        shape = RoundedCornerShape(8.dp),
                    ),
                rows = rows,
                columns = columns,
            ) {
                gridItemsByPage[page]?.forEach { gridItem ->
                    GridItemContent(
                        gridItem = gridItem,
                        color = Color(textColor),
                        gridItemSource = gridItemSource,
                        drag = drag,
                    )
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
            dockGridItems.forEach { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    color = color,
                    gridItemSource = gridItemSource,
                    drag = drag,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    color: Color,
    gridItemSource: GridItemSource,
    drag: Drag,
) {
    key(gridItem.id){
        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id && drag == Drag.Dragging,
                        color = color,
                    ) {
                        ApplicationInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            color = color,
                        )
                    }
                }

                is GridItemData.Widget -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id && drag == Drag.Dragging,
                        color = color,
                    ) {
                        WidgetGridItem(modifier = gridItemModifier, data = data)
                    }
                }

                is GridItemData.ShortcutInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id && drag == Drag.Dragging,
                        color = color,
                    ) {
                        ShortcutInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            color = color,
                        )
                    }
                }

                is GridItemData.Folder -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id && drag == Drag.Dragging,
                        color = color,
                    ) {
                        FolderGridItem(
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

@Composable
private fun DragGridItem(
    modifier: Modifier,
    isDragging: Boolean,
    color: Color,
    content: @Composable () -> Unit,
) {
    if (isDragging) {
        Box(
            modifier = modifier.border(
                width = 1.dp,
                color = color,
                shape = RoundedCornerShape(8.dp),
            ),
        )
    } else {
        content()
    }
}