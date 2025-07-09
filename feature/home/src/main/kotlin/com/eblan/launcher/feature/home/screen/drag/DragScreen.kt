package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.framework.widgetmanager.AppWidgetManagerWrapper
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
    gridItemSource: GridItemSource?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    movedGridItems: Boolean?,
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
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val density = LocalDensity.current

    val context = LocalContext.current

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

    val horizontalPagerPaddingDp = 20.dp

    val gridPaddingDp = 5.dp

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

    val widgetPreviewFallback = remember {
        gridItemSource?.imageBitmap?.asAndroidBitmap()?.toDrawable(context.resources)
    }

    val configureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleConfigureLauncherResult(
            targetPage = targetPage,
            result = result,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
            onDragEnd = onDragEnd,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onDeleteGridItem = onDeleteGridItem,
        )
    }

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        handleAppWidgetLauncherResult(
            targetPage = targetPage,
            result = result,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
            onDragEnd = onDragEnd,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onDeleteGridItem = onDeleteGridItem,
            onConfigure = configureLauncher::launch,
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            targetPage = targetPage,
            gridItems = gridItems[targetPage].orEmpty(),
            dockGridItems = dockGridItems,
            drag = drag,
            gridItemLayoutInfo = gridItemSource?.gridItemLayoutInfo,
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
        handleDrag(
            targetPage = targetPage,
            drag = drag,
            gridItemSource = gridItemSource,
            onDragEnd = onDragEnd,
            movedGridItems = movedGridItems,
            appWidgetManager = appWidgetManager,
            appWidgetLauncher = appWidgetLauncher,
            onDragCancel = onDragCancel,
            onDeleteAppWidgetId = onDeleteAppWidgetId,
            onConfigure = configureLauncher::launch,
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
                                .fillMaxSize()

                            when (val data = gridItem.data) {
                                is GridItemData.ApplicationInfo -> {
                                    Column(
                                        modifier = gridItemModifier,
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        AsyncImage(
                                            model = data.icon,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp, 40.dp)
                                                .weight(1f),
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = data.label.toString(),
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
                                        appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

                                    if (appWidgetInfo != null) {
                                        AndroidView(
                                            factory = {
                                                appWidgetHost.createView(
                                                    appWidgetId = data.appWidgetId,
                                                    appWidgetProviderInfo = appWidgetInfo,
                                                ).apply {
                                                    layoutParams = FrameLayout.LayoutParams(
                                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                                    )

                                                    setAppWidget(appWidgetId, appWidgetInfo)
                                                }
                                            },
                                            modifier = gridItemModifier,
                                        )
                                    } else if (data.preview != null) {
                                        AsyncImage(
                                            model = data.preview,
                                            contentDescription = null,
                                            modifier = gridItemModifier,
                                        )
                                    } else {
                                        AsyncImage(
                                            model = widgetPreviewFallback,
                                            contentDescription = null,
                                            modifier = gridItemModifier,
                                        )
                                    }
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
                            .fillMaxSize()

                        when (val gridItemData = dockGridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                Column(
                                    modifier = gridItemModifier,
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
                                        modifier = gridItemModifier,
                                    )
                                } else if (gridItemData.preview != null) {
                                    AsyncImage(
                                        model = gridItemData.preview,
                                        contentDescription = null,
                                        modifier = gridItemModifier,
                                    )
                                } else {
                                    AsyncImage(
                                        model = widgetPreviewFallback,
                                        contentDescription = null,
                                        modifier = gridItemModifier,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun handleDrag(
    targetPage: Int,
    drag: Drag,
    gridItemSource: GridItemSource?,
    onDragEnd: (Int) -> Unit,
    movedGridItems: Boolean?,
    appWidgetManager: AppWidgetManagerWrapper,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDragCancel: () -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    when (drag) {
        Drag.End -> {
            handleOnDragEnd(
                gridItemSource = gridItemSource,
                targetPage = targetPage,
                movedGridItems = movedGridItems,
                appWidgetManager = appWidgetManager,
                appWidgetLauncher = appWidgetLauncher,
                onDragEnd = onDragEnd,
                onDeleteAppWidgetId = onDeleteAppWidgetId,
                onConfigure = onConfigure,
            )
        }

        Drag.Cancel -> {
            onDragCancel()
        }

        Drag.Start, Drag.Dragging, Drag.None -> Unit
    }
}

private fun handleOnDragEnd(
    gridItemSource: GridItemSource?,
    targetPage: Int,
    movedGridItems: Boolean?,
    appWidgetManager: AppWidgetManagerWrapper,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    when (gridItemSource?.type) {
        GridItemSource.Type.New -> {
            when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    onDragEnd(targetPage)
                }

                is GridItemData.Widget -> {
                    onDragEndGridItemDataWidget(
                        targetPage = targetPage,
                        movedGridItems = movedGridItems,
                        appWidgetManager = appWidgetManager,
                        data = data,
                        onDragEnd = onDragEnd,
                        appWidgetLauncher = appWidgetLauncher,
                        onDeleteAppWidgetId = onDeleteAppWidgetId,
                        onConfigure = onConfigure,
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
    movedGridItems: Boolean?,
    appWidgetManager: AppWidgetManagerWrapper,
    data: GridItemData.Widget,
    onDragEnd: (Int) -> Unit,
    appWidgetLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onDeleteAppWidgetId: (Int) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    if (movedGridItems == true) {
        val provider = ComponentName.unflattenFromString(data.componentName)

        if (appWidgetManager.bindAppWidgetIdIfAllowed(
                appWidgetId = data.appWidgetId,
                provider = provider,
            )
        ) {
            configureComponent(
                appWidgetId = data.appWidgetId,
                data = data,
                targetPage = targetPage,
                onConfigure = onConfigure,
                onDragEnd = onDragEnd,
            )
        } else {
            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    data.appWidgetId,
                )
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,
                    provider,
                )
            }

            appWidgetLauncher.launch(intent)
        }
    } else {
        onDeleteAppWidgetId(data.appWidgetId)

        onDragEnd(targetPage)
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

private suspend fun handleDragIntOffset(
    targetPage: Int,
    gridItems: List<GridItem>,
    dockGridItems: List<GridItem>,
    drag: Drag,
    gridItemLayoutInfo: GridItemLayoutInfo?,
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
    if (drag == Drag.Dragging && gridItemLayoutInfo != null && !isScrollInProgress) {
        val isDraggingOnDock =
            dragIntOffset.y > (rootHeight - dockHeight) - gridPadding

        val scrollToPageDelay = 500L

        val moveGridItemDelay = 100L

        if (dragIntOffset.x <= gridPadding && !isDraggingOnDock) {
            delay(scrollToPageDelay)

            onChangePageDirection(PageDirection.Left)
        } else if (dragIntOffset.x >= rootWidth - gridPadding && !isDraggingOnDock) {
            delay(scrollToPageDelay)

            onChangePageDirection(PageDirection.Right)
        } else {
            if (isDraggingOnDock) {
                delay(moveGridItemDelay)

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
                    gridItems + dockGridItems,
                    gridItem,
                    dragIntOffset.x,
                    dockY,
                    dockRows,
                    dockColumns,
                    rootWidth,
                    dockHeight,
                )
            } else {
                delay(moveGridItemDelay)

                val gridWidth = rootWidth - (gridPadding * 2)

                val gridHeight = (rootHeight - dockHeight) - (gridPadding * 2)

                val gridX = dragIntOffset.x - gridPadding

                val gridY = dragIntOffset.y - gridPadding

                val cellWidth = gridWidth / columns

                val cellHeight = gridHeight / rows

                val gridItem = gridItemLayoutInfo.gridItem.copy(
                    page = targetPage,
                    startRow = gridY / cellHeight,
                    startColumn = gridX / cellWidth,
                    associate = Associate.Grid,
                )

                onMoveGridItem(
                    gridItems + dockGridItems,
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

private fun handleAppWidgetLauncherResult(
    targetPage: Int,
    result: ActivityResult,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onConfigure: (Intent) -> Unit,
) {
    when (val data = gridItemLayoutInfo?.gridItem?.data) {
        is GridItemData.Widget -> {
            if (result.resultCode == Activity.RESULT_OK) {
                val appWidgetId =
                    result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

                configureComponent(
                    appWidgetId = appWidgetId,
                    data = data,
                    targetPage = targetPage,
                    onConfigure = onConfigure,
                    onDragEnd = onDragEnd,
                )
            } else {
                onDeleteAppWidgetId(data.appWidgetId)

                onDeleteGridItem(gridItemLayoutInfo.gridItem)

                onDragEnd(targetPage)
            }
        }

        else -> Unit
    }
}

private fun configureComponent(
    appWidgetId: Int,
    data: GridItemData.Widget,
    targetPage: Int,
    onConfigure: (Intent) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val configureComponent = data.configure?.let(ComponentName::unflattenFromString)

    if (configureComponent != null) {
        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)

        intent.component = configureComponent

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

        onConfigure(intent)
    } else {
        onDragEnd(targetPage)
    }
}

private fun handleConfigureLauncherResult(
    targetPage: Int,
    result: ActivityResult,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    onDragEnd: (Int) -> Unit,
    onDeleteAppWidgetId: (Int) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
) {
    when (gridItemLayoutInfo?.gridItem?.data) {
        is GridItemData.Widget -> {
            val appWidgetId =
                result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1

            if (result.resultCode == Activity.RESULT_OK) {
                onDragEnd(targetPage)
            } else {
                onDeleteAppWidgetId(appWidgetId)

                onDeleteGridItem(gridItemLayoutInfo.gridItem)

                onDragEnd(targetPage)
            }
        }

        else -> Unit
    }
}