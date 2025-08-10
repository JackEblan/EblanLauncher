package com.eblan.launcher.feature.home.screen.drag

import android.appwidget.AppWidgetManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    moveGridItemResult: MoveGridItemResult?,
    updatedGridItem: GridItem?,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onDragCancel: (Int) -> Unit,
    onDragEndAfterMove: (
        targetPage: Int,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) -> Unit,
    onMoveGridItemsFailed: (Int) -> Unit,
    onDeleteGridItemCache: (GridItem) -> Unit,
    onUpdateGridItemDataCache: (GridItem) -> Unit,
    onDeleteWidgetGridItemCache: (
        targetPage: Int,
        gridItem: GridItem,
        appWidgetId: Int,
    ) -> Unit,
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

    val targetPage by remember {
        derivedStateOf {
            calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )
        }
    }

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureResult(
            targetPage = targetPage,
            moveGridItemResult = moveGridItemResult,
            updatedGridItem = updatedGridItem,
            resultCode = result.resultCode,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
            onDragEndAfterMove = onDragEndAfterMove,
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
            targetPage = targetPage,
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            gridPadding = gridPaddingPx,
            rootWidth = rootWidth,
            dockColumns = dockColumns,
            dockRows = dockRows,
            rows = rows,
            columns = columns,
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
                    targetPage = targetPage,
                    moveGridItemResult = moveGridItemResult,
                    androidAppWidgetHostWrapper = appWidgetHostWrapper,
                    appWidgetManager = appWidgetManager,
                    gridItemSource = gridItemSource,
                    onLaunch = appWidgetLauncher::launch,
                    onDragEndAfterMove = onDragEndAfterMove,
                    onMoveGridItemsFailed = onMoveGridItemsFailed,
                    onDeleteGridItemCache = onDeleteGridItemCache,
                    onUpdateGridItemDataCache = onUpdateGridItemDataCache,
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                )
            }

            Drag.Cancel -> {
                onDragCancel(targetPage)
            }

            else -> Unit
        }
    }

    LaunchedEffect(key1 = deleteAppWidgetId) {
        handleDeleteAppWidgetId(
            targetPage = targetPage,
            gridItem = gridItemSource.gridItem,
            appWidgetId = lastAppWidgetId,
            deleteAppWidgetId = deleteAppWidgetId,
            onDeleteWidgetGridItemCache = onDeleteWidgetGridItemCache,
        )
    }

    LaunchedEffect(key1 = updatedGridItem) {
        handleBoundWidget(
            targetPage = targetPage,
            gridItemSource = gridItemSource,
            updatedGridItem = updatedGridItem,
            moveGridItemResult = moveGridItemResult,
            onConfigure = configureLauncher::launch,
            onDeleteGridItemCache = onDeleteGridItemCache,
            onDragEndAfterMove = onDragEndAfterMove,
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
                        color = Color(textColor).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = Color(textColor),
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
                    color = Color(textColor),
                    gridItemSource = gridItemSource,
                )
            }
        }
    }

    if (moveGridItemResult != null &&
        moveGridItemResult.isSuccess &&
        moveGridItemResult.conflictingGridItem == null &&
        drag == Drag.End
    ) {
        AnimatedDropGridItem(
            moveGridItemResult = moveGridItemResult,
            gridPaddingPx = gridPaddingPx,
            rootWidth = rootWidth,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            rows = rows,
            columns = columns,
            dockRows = dockRows,
            dockColumns = dockColumns,
            dragIntOffset = dragIntOffset,
            density = density,
            textColor = textColor,
        )
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    color: Color,
    gridItemSource: GridItemSource,
) {
    key(gridItem.id) {
        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
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
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = color,
                    ) {
                        WidgetGridItem(modifier = gridItemModifier, data = data)
                    }
                }

                is GridItemData.ShortcutInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
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
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
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
private fun AnimatedDropGridItem(
    moveGridItemResult: MoveGridItemResult,
    gridPaddingPx: Int,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    rows: Int,
    columns: Int,
    dockRows: Int,
    dockColumns: Int,
    dragIntOffset: IntOffset,
    density: Density,
    textColor: Long,
) {
    when (moveGridItemResult.movingGridItem.associate) {
        Associate.Grid -> {
            val gridWidth = rootWidth - (gridPaddingPx * 2)

            val gridHeight = (rootHeight - dockHeight) - (gridPaddingPx * 2)

            val cellWidth = gridWidth / columns

            val cellHeight = gridHeight / rows

            val x = moveGridItemResult.movingGridItem.startColumn * cellWidth

            val y = moveGridItemResult.movingGridItem.startRow * cellHeight

            val width = moveGridItemResult.movingGridItem.columnSpan * cellWidth

            val height = moveGridItemResult.movingGridItem.rowSpan * cellHeight

            val shadowX = dragIntOffset.x - (width / 2)

            val shadowY = dragIntOffset.y - (height / 2)

            val animatedX = remember {
                Animatable(shadowX.toFloat())
            }

            val animatedY = remember {
                Animatable(shadowY.toFloat())
            }

            LaunchedEffect(key1 = moveGridItemResult) {
                launch {
                    animatedX.animateTo(x.toFloat() + gridPaddingPx)
                }

                launch {
                    animatedY.animateTo(y.toFloat() + gridPaddingPx)
                }
            }

            val size = with(density) {
                DpSize(width = width.toDp(), height = height.toDp())
            }

            val gridItemModifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedX.value.roundToInt(),
                        y = animatedY.value.roundToInt(),
                    )
                }
                .size(size)

            when (val data = moveGridItemResult.movingGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = Color(textColor),
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
                        color = Color(textColor),
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = Color(textColor),
                    )
                }
            }
        }

        Associate.Dock -> {
            val cellWidth = rootWidth / dockColumns

            val cellHeight = dockHeight / dockRows

            val x = moveGridItemResult.movingGridItem.startColumn * cellWidth

            val width = moveGridItemResult.movingGridItem.columnSpan * cellWidth

            val height = moveGridItemResult.movingGridItem.rowSpan * cellHeight

            val shadowX = dragIntOffset.x - (width / 2)

            val shadowY = dragIntOffset.y - (height / 2)

            val animatedX = remember {
                Animatable(shadowX.toFloat())
            }

            val animatedY = remember {
                Animatable(shadowY.toFloat())
            }

            LaunchedEffect(key1 = animatedX) {
                animatedX.animateTo(x.toFloat())
            }

            LaunchedEffect(key1 = animatedY) {
                animatedY.animateTo((rootHeight - dockHeight).toFloat())
            }

            val size = with(density) {
                DpSize(width = width.toDp(), height = height.toDp())
            }

            val gridItemModifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedX.value.roundToInt(),
                        y = animatedY.value.roundToInt(),
                    )
                }
                .size(size)

            when (val data = moveGridItemResult.movingGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = Color(textColor),
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
                        color = Color(textColor),
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = Color(textColor),
                    )
                }
            }
        }
    }
}

@Composable
fun DragGridItem(
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