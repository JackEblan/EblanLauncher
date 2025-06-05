package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
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
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItemMenu
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItemMenu
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.pager.GridItemMenu
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerController
import kotlinx.coroutines.delay

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
    shiftedAlgorithm: Boolean?,
    addNewPage: Boolean,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
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
    onResize: (Int) -> Unit,
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

    val tempPageCount = if (addNewPage) pageCount + 1 else pageCount

    val horizontalPagerState = rememberPagerState(
        initialPage = if (infiniteScroll) (Int.MAX_VALUE / 2) + currentPage else currentPage,
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

    val targetPage by remember {
        derivedStateOf {
            calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = infiniteScroll,
                pageCount = tempPageCount,
            )
        }
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleResult(
            targetPage = targetPage,
            result = result,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            onDragEnd = onDragEnd,
            appWidgetHost = appWidgetHost,
            onDeleteGridItem = onDeleteGridItem,
        )
    }

    var selectedGridItem by remember { mutableStateOf<GridItem?>(null) }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            targetPage = targetPage,
            drag = drag,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
            dragIntOffset = dragIntOffset,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            horizontalPagerPaddingPx = horizontalPagerPaddingPx,
            rootWidth = rootWidth,
            dockColumns = dockColumns,
            dockRows = dockRows,
            columns = columns,
            rows = rows,
            onChangePageDirection = { newPageDirection ->
                pageDirection = newPageDirection
            },
            onMoveGridItem = onMoveGridItem,
        )
    }


    LaunchedEffect(key1 = pageDirection) {
        handlePageDirection(
            pageDirection = pageDirection,
            horizontalPagerState = horizontalPagerState,
        )
    }

    LaunchedEffect(key1 = drag) {
        handleDrag(
            targetPage = targetPage,
            drag = drag,
            showMenu = showMenu,
            gridItemSource = gridItemSource,
            onDragEnd = onDragEnd,
            shiftedAlgorithm = shiftedAlgorithm,
            appWidgetHost = appWidgetHost,
            appWidgetManager = appWidgetManager,
            onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            appWidgetLauncher = appWidgetLauncher,
            onDragCancel = onDragCancel,
            onChangeShowMenu = { newShowMenu ->
                showMenu = newShowMenu
            },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GridItemOverlay(
            isScrollInProgress = horizontalPagerState.isScrollInProgress,
            selectedGridItem = selectedGridItem,
            rootWidth = rootWidth,
            horizontalPagerPaddingPx = horizontalPagerPaddingPx,
            rootHeight = rootHeight,
            dockHeight = dockHeight,
            columns = columns,
            rows = rows,
            density = density,
            dockColumns = dockColumns,
            dockRows = dockRows,
        )

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

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(cardPadding),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                    border = BorderStroke(width = 2.dp, color = Color.White),
                ) {
                    DragGridSubcomposeLayout(
                        modifier = Modifier.fillMaxSize(),
                        index = horizontalPage,
                        rows = rows,
                        columns = columns,
                        gridItems = gridItems,
                        gridItemContent = { gridItem ->
                            if (gridItemSource?.gridItemLayoutInfo?.gridItem?.id != gridItem.id) {
                                when (val gridItemData = gridItem.data) {
                                    is GridItemData.ApplicationInfo -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .border(width = 1.dp, color = Color.White),
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
                                            )
                                        }
                                    }
                                }
                            } else {
                                selectedGridItem = gridItem
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
                if (gridItemSource?.gridItemLayoutInfo?.gridItem?.id != dockGridItem.id) {
                    when (val gridItemData = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .border(width = 1.dp, color = Color.White),
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
                                )
                            }
                        }
                    }
                } else {
                    selectedGridItem = dockGridItem
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
                                        onResize = {
                                            val horizontalPage = calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = tempPageCount,
                                            )

                                            onResize(horizontalPage)
                                        },
                                    )
                                }

                                is GridItemData.Widget -> {
                                    val showResize =
                                        gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                    WidgetGridItemMenu(
                                        showResize = showResize,
                                        onEdit = onEdit,
                                        onResize = {
                                            val horizontalPage = calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = tempPageCount,
                                            )

                                            onResize(horizontalPage)
                                        },
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
                                        onResize = {
                                            val horizontalPage = calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = tempPageCount,
                                            )

                                            onResize(horizontalPage)
                                        },
                                    )
                                }

                                is GridItemData.Widget -> {
                                    val showResize =
                                        gridItemSource.gridItemLayoutInfo.gridItem.associate == Associate.Grid && data.resizeMode != AppWidgetProviderInfo.RESIZE_NONE

                                    WidgetGridItemMenu(
                                        showResize = showResize,
                                        onEdit = onEdit,
                                        onResize = {
                                            val horizontalPage = calculatePage(
                                                index = horizontalPagerState.currentPage,
                                                infiniteScroll = infiniteScroll,
                                                pageCount = tempPageCount,
                                            )

                                            onResize(horizontalPage)
                                        },
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

@Composable
private fun GridItemOverlay(
    isScrollInProgress: Boolean,
    selectedGridItem: GridItem?,
    rootWidth: Int,
    horizontalPagerPaddingPx: Int,
    rootHeight: Int,
    dockHeight: Int,
    columns: Int,
    rows: Int,
    density: Density,
    dockColumns: Int,
    dockRows: Int,
) {
    if (selectedGridItem != null) {
        var x by remember { mutableIntStateOf(0) }

        var y by remember { mutableIntStateOf(0) }

        var size by remember { mutableStateOf(DpSize.Zero) }

        when (selectedGridItem.associate) {
            Associate.Grid -> {
                val gridWidth = rootWidth - (horizontalPagerPaddingPx * 2)

                val gridHeight = (rootHeight - dockHeight) - (horizontalPagerPaddingPx * 2)

                val cellWidth = gridWidth / columns

                val cellHeight = gridHeight / rows

                x = (selectedGridItem.startColumn * cellWidth) + horizontalPagerPaddingPx

                y = (selectedGridItem.startRow * cellHeight) + horizontalPagerPaddingPx

                size = with(density) {
                    DpSize(
                        width = (selectedGridItem.columnSpan * cellWidth).toDp(),
                        height = (selectedGridItem.rowSpan * cellHeight).toDp(),
                    )
                }
            }

            Associate.Dock -> {
                val cellWidth = rootWidth / dockColumns

                val cellHeight = dockHeight / dockRows

                x = selectedGridItem.startColumn * cellWidth

                y = (selectedGridItem.startRow * cellHeight) + (rootHeight - dockHeight)

                size = with(density) {
                    DpSize(
                        width = (selectedGridItem.columnSpan * cellWidth).toDp(),
                        height = (selectedGridItem.rowSpan * cellHeight).toDp(),
                    )
                }
            }
        }

        val offset by animateIntOffsetAsState(targetValue = IntOffset(x = x, y = y))

        if (!isScrollInProgress) {
            Box(
                modifier = Modifier
                    .offset {
                        offset
                    }
                    .size(size)
                    .border(width = 1.dp, color = Color.White),
            )
        }
    }
}

private fun handleDrag(
    targetPage: Int,
    drag: Drag,
    showMenu: Boolean,
    gridItemSource: GridItemSource?,
    onDragEnd: (Int) -> Unit,
    shiftedAlgorithm: Boolean?,
    appWidgetHost: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerController,
    onUpdateWidgetGridItem: (id: String, data: GridItemData, appWidgetId: Int) -> Unit,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDragCancel: () -> Unit,
    onChangeShowMenu: (Boolean) -> Unit,
) {
    when (drag) {
        Drag.Start -> {
            onChangeShowMenu(true)
        }

        Drag.End -> {
            handleOnDragEnd(
                showMenu = showMenu,
                gridItemSource = gridItemSource,
                targetPage = targetPage,
                shiftedAlgorithm = shiftedAlgorithm,
                appWidgetHost = appWidgetHost,
                appWidgetManager = appWidgetManager,
                appWidgetLauncher = appWidgetLauncher,
                onDragEnd = onDragEnd,
                onUpdateWidgetGridItem = onUpdateWidgetGridItem,
            )
        }

        Drag.Dragging -> {
            onChangeShowMenu(false)
        }

        Drag.Cancel -> {
            onDragCancel()
        }

        Drag.None -> {

        }
    }
}

private fun handleOnDragEnd(
    showMenu: Boolean,
    gridItemSource: GridItemSource?,
    targetPage: Int,
    shiftedAlgorithm: Boolean?,
    appWidgetHost: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerController,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDragEnd: (Int) -> Unit,
    onUpdateWidgetGridItem: (id: String, data: GridItemData, appWidgetId: Int) -> Unit,
) {
    if (showMenu) {
        return
    }

    when (gridItemSource?.type) {
        GridItemSource.Type.New -> {
            when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    onDragEnd(targetPage)
                }

                is GridItemData.Widget -> {
                    onDragEndGridItemDataWidget(
                        targetPage = targetPage,
                        gridItemLayoutInfo = gridItemSource.gridItemLayoutInfo,
                        shiftedAlgorithm = shiftedAlgorithm,
                        appWidgetHost = appWidgetHost,
                        appWidgetManager = appWidgetManager,
                        data = data,
                        onUpdateWidgetGridItem = onUpdateWidgetGridItem,
                        onDragEnd = onDragEnd,
                        appWidgetLauncher = appWidgetLauncher,
                    )
                }
            }
        }

        GridItemSource.Type.Old -> {
            onDragEnd(targetPage)
        }

        null -> Unit
    }
}

private fun onDragEndGridItemDataWidget(
    targetPage: Int,
    gridItemLayoutInfo: GridItemLayoutInfo,
    shiftedAlgorithm: Boolean?,
    appWidgetHost: AppWidgetHostWrapper,
    appWidgetManager: AppWidgetManagerController,
    data: GridItemData.Widget,
    onUpdateWidgetGridItem: (id: String, data: GridItemData, appWidgetId: Int) -> Unit,
    onDragEnd: (Int) -> Unit,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
) {
    if (shiftedAlgorithm != null && shiftedAlgorithm) {
        val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

        val provider = ComponentName.unflattenFromString(data.componentName)

        if (appWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId = allocateAppWidgetId,
                provider = provider,
            )
        ) {
            onUpdateWidgetGridItem(
                gridItemLayoutInfo.gridItem.id,
                gridItemLayoutInfo.gridItem.data,
                allocateAppWidgetId,
            )

            onDragEnd(targetPage)
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
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
        onDragEnd(targetPage)
    }
}

private suspend fun handlePageDirection(
    pageDirection: PageDirection?,
    horizontalPagerState: PagerState,
) {
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

private fun handleDragIntOffset(
    targetPage: Int,
    drag: Drag,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dragIntOffset: IntOffset,
    rootHeight: Int,
    dockHeight: Int,
    horizontalPagerPaddingPx: Int,
    rootWidth: Int,
    dockColumns: Int,
    dockRows: Int,
    columns: Int,
    rows: Int,
    onChangePageDirection: (PageDirection?) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    if (drag == Drag.Dragging && gridItemLayoutInfo != null) {
        val isDraggingOnDock = dragIntOffset.y > rootHeight - dockHeight

        if (dragIntOffset.x < horizontalPagerPaddingPx && !isDraggingOnDock) {
            onChangePageDirection(PageDirection.Left)
        } else if (dragIntOffset.x > rootWidth - horizontalPagerPaddingPx && !isDraggingOnDock) {
            onChangePageDirection(PageDirection.Right)
        } else if (isDraggingOnDock) {
            onChangePageDirection(null)

            val cellWidth = rootWidth / dockColumns

            val cellHeight = dockHeight / dockRows

            val dockY = dragIntOffset.y - (rootHeight - dockHeight)

            val gridItem = gridItemLayoutInfo.gridItem.copy(
                page = targetPage,
                startRow = dockY / cellHeight,
                startColumn = dragIntOffset.x / cellWidth,
                associate = Associate.Dock,
            )

            onMoveGridItem(
                gridItem,
                dragIntOffset.x,
                dockY,
                dockRows,
                dockColumns,
                rootWidth,
                dockHeight,
            )
        } else {
            onChangePageDirection(null)

            val gridWidth = rootWidth - (horizontalPagerPaddingPx * 2)

            val gridHeight = (rootHeight - dockHeight) - (horizontalPagerPaddingPx * 2)

            val gridX = dragIntOffset.x - horizontalPagerPaddingPx

            val gridY = dragIntOffset.y - horizontalPagerPaddingPx

            val insideGridVerticalPadding =
                dragIntOffset.y >= horizontalPagerPaddingPx && dragIntOffset.y <= horizontalPagerPaddingPx + gridHeight

            if (insideGridVerticalPadding) {
                val cellWidth = gridWidth / columns

                val cellHeight = gridHeight / rows

                val gridItem = gridItemLayoutInfo.gridItem.copy(
                    page = targetPage,
                    startRow = gridY / cellHeight,
                    startColumn = gridX / cellWidth,
                    associate = Associate.Grid,
                )

                onMoveGridItem(
                    gridItem,
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
}

private fun handleResult(
    targetPage: Int,
    result: ActivityResult,
    appWidgetHost: AppWidgetHostWrapper,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onUpdateWidgetGridItem: (id: String, data: GridItemData, appWidgetId: Int) -> Unit,
    onDragEnd: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    val appWidgetId = result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

    if (result.resultCode == Activity.RESULT_OK) {
        if (gridItemLayoutInfo != null && appWidgetId > 0) {
            onUpdateWidgetGridItem(
                gridItemLayoutInfo.gridItem.id,
                gridItemLayoutInfo.gridItem.data,
                appWidgetId,
            )

            onDragEnd(targetPage)
        }
    } else {
        appWidgetHost.deleteAppWidgetId(appWidgetId = appWidgetId)

        if (gridItemLayoutInfo != null) {
            onDeleteGridItem(gridItemLayoutInfo.gridItem)

            onDragEnd(targetPage)
        }
    }
}