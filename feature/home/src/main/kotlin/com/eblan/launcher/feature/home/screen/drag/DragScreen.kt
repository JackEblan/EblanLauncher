package com.eblan.launcher.feature.home.screen.drag

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
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
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    movedGridItems: Boolean,
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
    onDeleteGridItem: (GridItem) -> Unit,
    onUpdatePinWidget: (
        id: Int,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteWidgetGridItem: (Int) -> Unit,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

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

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            result = result,
            gridItemSource = gridItemSource,
            onUpdatePinWidget = onUpdatePinWidget,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
            onDragEnd = onDragEnd,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            result = result,
            gridItemSource = gridItemSource,
            onDragEnd = onDragEnd,
            onConfigure = configureLauncher::launch,
            onUpdatePinWidget = onUpdatePinWidget,
            onDeleteWidgetGridItem = onDeleteWidgetGridItem,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            currentPage = horizontalPagerState.currentPage,
            infiniteScroll = infiniteScroll,
            pageCount = pageCount,
            gridItems = gridItems,
            dockGridItems = dockGridItems,
            drag = drag,
            gridItem = gridItemSource?.gridItem,
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
        when (gridItemSource) {
            is GridItemSource.New, is GridItemSource.Pin -> {
                handleDragNew(
                    currentPage = horizontalPagerState.currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = pageCount,
                    drag = drag,
                    gridItemSource = gridItemSource,
                    movedGridItems = movedGridItems,
                    appWidgetHostWrapper = appWidgetHost,
                    appWidgetManager = appWidgetManager,
                    onDragCancel = onDragCancel,
                    onConfigure = configureLauncher::launch,
                    onDragEnd = onDragEnd,
                    onDeleteGridItem = onDeleteGridItem,
                    onUpdatePinWidget = onUpdatePinWidget,
                    onDeleteWidgetGridItem = onDeleteWidgetGridItem,
                    onLaunch = appWidgetLauncher::launch,
                )

            }

            is GridItemSource.Existing -> {
                handleDragExisting(
                    drag,
                    horizontalPagerState.currentPage,
                    infiniteScroll,
                    pageCount,
                    onDragEnd,
                    onDragCancel,
                )
            }

            null -> Unit
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
                    GridItemContent(
                        gridItem = gridItem,
                        color = color,
                        gridItemSource = gridItemSource,
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
    gridItemSource: GridItemSource?,
) {
    key(gridItem.id) {
        LookaheadScope {
            val gridItemModifier = modifier
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
                    DragWidgetGridItem(
                        modifier = gridItemModifier,
                        id = gridItem.id,
                        gridItemSource = gridItemSource,
                        data = data,
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    DragShortcutInfoGridItem(
                        modifier = gridItemModifier,
                        id = gridItem.id,
                        gridItemSource = gridItemSource,
                        data = data,
                        color = color,
                    )
                }
            }
        }
    }
}


@Composable
private fun DragWidgetGridItem(
    modifier: Modifier = Modifier,
    id: Int,
    gridItemSource: GridItemSource?,
    data: GridItemData.Widget,
) {
    if (gridItemSource?.gridItem?.id == id &&
        gridItemSource is GridItemSource.Pin
    ) {
        AsyncImage(
            model = gridItemSource.byteArray,
            contentDescription = null,
            modifier = modifier,
        )
    } else {
        WidgetGridItem(modifier = modifier, data = data)
    }
}


@Composable
private fun DragShortcutInfoGridItem(
    modifier: Modifier,
    id: Int,
    gridItemSource: GridItemSource?,
    data: GridItemData.ShortcutInfo,
    color: Color,
) {
    if (gridItemSource?.gridItem?.id == id &&
        gridItemSource is GridItemSource.Pin
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = gridItemSource.byteArray,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp, 40.dp)
                    .weight(1f),
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = data.shortLabel,
                modifier = Modifier.weight(1f),
                color = color,
                textAlign = TextAlign.Center,
                fontSize = TextUnit(
                    value = 10f,
                    type = TextUnitType.Sp,
                ),
            )
        }
    } else {
        ShortcutInfoGridItem(
            modifier = modifier,
            data = data,
            color = color,
        )
    }
}