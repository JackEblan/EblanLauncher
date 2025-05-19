package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.MoveGridItem
import com.eblan.launcher.feature.home.screen.pager.GridItemMenu
import com.eblan.launcher.feature.home.util.calculatePage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    targetPage: Int,
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
    shiftedAlgorithm: Boolean?,
    onMoveGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: (Int) -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var showMenu by remember { mutableStateOf(false) }

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    var moveGridItem by remember { mutableStateOf<MoveGridItem?>(null) }

    val tempPageCount = pageCount + 1

    val horizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + targetPage else targetPage,
        pageCount = {
            if (infiniteScroll) {
                Int.MAX_VALUE
            } else {
                tempPageCount
            }
        },
    )

    val horizontalPagerPadding = 20.dp

    val cardPadding = 5.dp

    val horizontalPagerPaddingPx = with(density) {
        (horizontalPagerPadding + cardPadding).roundToPx()
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

        if (result.resultCode == Activity.RESULT_OK) {
            if (gridItemSource?.gridItemLayoutInfo != null && appWidgetId > 0) {
                onUpdateWidgetGridItem(
                    gridItemSource.gridItemLayoutInfo.gridItem.id,
                    gridItemSource.gridItemLayoutInfo.gridItem.data,
                    appWidgetId,
                )

                val horizontalPage = calculatePage(
                    index = horizontalPagerState.currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = tempPageCount,
                )

                onDragEnd(horizontalPage)
            }
        } else {
            appWidgetHost.deleteAppWidgetId(appWidgetId = appWidgetId)

            if (gridItemSource?.gridItemLayoutInfo != null) {
                onDeleteGridItem(gridItemSource.gridItemLayoutInfo.gridItem)

                val horizontalPage = calculatePage(
                    index = horizontalPagerState.currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = tempPageCount,
                )

                onDragEnd(horizontalPage)
            }
        }
    }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                delay(1000L)

                horizontalPagerState.animateScrollToPage(horizontalPagerState.currentPage - 1)
            }

            PageDirection.Right -> {
                delay(1000L)

                horizontalPagerState.animateScrollToPage(horizontalPagerState.currentPage + 1)
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.Start -> {
                showMenu = true
            }

            Drag.End -> {
                if (!showMenu) {
                    when (gridItemSource?.type) {
                        GridItemSource.Type.New -> {
                            when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    val horizontalPage = calculatePage(
                                        index = horizontalPagerState.currentPage,
                                        infiniteScroll = infiniteScroll,
                                        pageCount = tempPageCount,
                                    )

                                    onDragEnd(horizontalPage)
                                }

                                is GridItemData.Widget -> {
                                    if (shiftedAlgorithm != null && shiftedAlgorithm) {
                                        val allocateAppWidgetId =
                                            appWidgetHost.allocateAppWidgetId()

                                        val provider =
                                            ComponentName.unflattenFromString(data.componentName)

                                        if (appWidgetManager.bindAppWidgetIdIfAllowed(
                                                appWidgetId = allocateAppWidgetId,
                                                provider = provider,
                                            )
                                        ) {
                                            onUpdateWidgetGridItem(
                                                gridItemSource.gridItemLayoutInfo.gridItem.id,
                                                gridItemSource.gridItemLayoutInfo.gridItem.data,
                                                allocateAppWidgetId,
                                            )

                                            val horizontalPage = calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = tempPageCount,
                                            )

                                            onDragEnd(horizontalPage)
                                        } else {
                                            val intent =
                                                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                                                    putExtra(
                                                        AppWidgetManager.EXTRA_APPWIDGET_ID,
                                                        allocateAppWidgetId,
                                                    )
                                                    putExtra(
                                                        AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                                                        provider,
                                                    )
                                                }

                                            appWidgetLauncher.launch(intent)
                                        }
                                    } else {
                                        val horizontalPage = calculatePage(
                                            index = horizontalPagerState.currentPage,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = tempPageCount,
                                        )

                                        onDragEnd(horizontalPage)
                                    }
                                }
                            }
                        }

                        GridItemSource.Type.Old -> {
                            val horizontalPage = calculatePage(
                                index = horizontalPagerState.currentPage,
                                infiniteScroll = infiniteScroll,
                                pageCount = tempPageCount,
                            )

                            onDragEnd(horizontalPage)
                        }

                        null -> Unit
                    }
                }
            }

            Drag.Dragging -> {
                showMenu = false
            }

            Drag.Cancel -> {
                onDragCancel()
            }

            Drag.None -> {

            }
        }
    }

    LaunchedEffect(key1 = moveGridItem) {
        snapshotFlow { moveGridItem }.debounce(500L).filterNotNull()
            .onEach { (gridItem, rows, columns) ->
                onMoveGridItem(gridItem, rows, columns)
            }.collect()
    }

    Box(modifier = modifier.fillMaxSize()) {
        LaunchedEffect(key1 = dragIntOffset) {
            if (drag == Drag.Dragging && gridItemSource?.gridItemLayoutInfo != null) {
                val isDraggingOnDock = dragIntOffset.y > rootHeight - dockHeight

                val horizontalPage = calculatePage(
                    index = horizontalPagerState.currentPage,
                    infiniteScroll = infiniteScroll,
                    pageCount = tempPageCount,
                )

                if (dragIntOffset.x < horizontalPagerPaddingPx && !isDraggingOnDock) {
                    pageDirection = PageDirection.Left
                } else if (dragIntOffset.x > rootWidth - horizontalPagerPaddingPx && !isDraggingOnDock) {
                    pageDirection = PageDirection.Right
                } else {
                    pageDirection = null

                    if (isDraggingOnDock) {
                        val cellWidth = rootWidth / dockColumns

                        val cellHeight = dockHeight / dockRows

                        val dockY = dragIntOffset.y - rootHeight

                        val gridItem = gridItemSource.gridItemLayoutInfo.gridItem.copy(
                            page = horizontalPage,
                            startRow = dockY / cellHeight,
                            startColumn = dragIntOffset.x / cellWidth,
                            associate = Associate.Dock,
                        )
                        moveGridItem = MoveGridItem(
                            gridItem = gridItem,
                            rows = dockRows,
                            columns = dockColumns,
                        )
                    } else {
                        val gridWidth = rootWidth - horizontalPagerPaddingPx

                        val gridHeight = rootHeight - (horizontalPagerPaddingPx + dockHeight)

                        val gridX = dragIntOffset.x - horizontalPagerPaddingPx

                        val gridY = dragIntOffset.y - horizontalPagerPaddingPx

                        val insideGrid =
                            dragIntOffset.x >= horizontalPagerPaddingPx && dragIntOffset.x <= horizontalPagerPaddingPx + gridWidth && dragIntOffset.y >= horizontalPagerPaddingPx && dragIntOffset.y <= horizontalPagerPaddingPx + gridHeight

                        if (insideGrid) {
                            val cellWidth = gridWidth / columns

                            val cellHeight = gridHeight / rows

                            val gridItem = gridItemSource.gridItemLayoutInfo.gridItem.copy(
                                page = horizontalPage,
                                startRow = gridY / cellHeight,
                                startColumn = gridX / cellWidth,
                                associate = Associate.Grid,
                            )

                            moveGridItem =
                                MoveGridItem(gridItem = gridItem, rows = rows, columns = columns)
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = horizontalPagerState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(all = horizontalPagerPadding),
            ) { index ->
                val horizontalPage = calculatePage(
                    index = index,
                    infiniteScroll = infiniteScroll,
                    pageCount = tempPageCount,
                )

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(cardPadding),
                ) {
                    DragGridSubcomposeLayout(
                        modifier = Modifier.fillMaxSize(),
                        index = horizontalPage,
                        rows = rows,
                        columns = columns,
                        gridItems = gridItems,
                        gridItemContent = { gridItem ->
                            if (gridItemSource?.gridItemLayoutInfo?.gridItem?.id == gridItem.id) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(width = 1.dp, color = Color.White),
                                )
                            } else {
                                when (val gridItemData = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
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
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                }
            }

            DockGrid(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dockHeightDp),
                rows = dockRows,
                columns = dockColumns,
                dockGridItems = dockGridItems,
            ) { dockGridItem, _, _, _, _ ->
                if (gridItemSource?.gridItemLayoutInfo?.gridItem?.id == dockGridItem.id) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(width = 1.dp, color = Color.White),
                    )
                } else {
                    when (val gridItemData = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
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
                                )
                            }
                        }
                    }
                }
            }
        }
        if (showMenu && gridItemSource?.gridItemLayoutInfo?.gridItem != null) {
            when (gridItemSource.gridItemLayoutInfo.gridItem.associate) {
                Associate.Grid -> {
                    GridItemMenu(
                        x = dragIntOffset.x - gridItemSource.gridItemLayoutInfo.width / 2,
                        y = dragIntOffset.y - gridItemSource.gridItemLayoutInfo.height / 2,
                        width = gridItemSource.gridItemLayoutInfo.width,
                        height = gridItemSource.gridItemLayoutInfo.height,
                        onDismissRequest = onDragCancel,
                        content = {
                            when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    ApplicationInfoGridItemMenu(
                                        showResize = gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                        onEdit = onEdit,
                                        onResize = onResize,
                                    )
                                }

                                is GridItemData.Widget -> {
                                    val showResize =
                                        gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                    WidgetGridItemMenu(
                                        showResize = showResize,
                                        onEdit = onEdit,
                                        onResize = onResize,
                                    )
                                }
                            }
                        },
                    )
                }

                Associate.Dock -> {
                    GridItemMenu(
                        x = gridItemSource.gridItemLayoutInfo.x,
                        y = rootHeight - dockHeight,
                        width = gridItemSource.gridItemLayoutInfo.width,
                        height = gridItemSource.gridItemLayoutInfo.height,
                        onDismissRequest = onDragCancel,
                        content = {
                            when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    ApplicationInfoGridItemMenu(
                                        showResize = gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid,
                                        onEdit = onEdit,
                                        onResize = onResize,
                                    )
                                }

                                is GridItemData.Widget -> {
                                    val showResize =
                                        gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                    WidgetGridItemMenu(
                                        showResize = showResize,
                                        onEdit = onEdit,
                                        onResize = onResize,
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}