package com.eblan.launcher.feature.home.screen.drag

import android.appwidget.AppWidgetManager
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.designsystem.local.LocalWallpaperManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleWallpaperScroll

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
    gridWidth: Int,
    gridHeight: Int,
    dockHeight: Int,
    paddingValues: PaddingValues,
    dockGridItems: List<GridItem>,
    textColor: Long,
    moveGridItemResult: MoveGridItemResult?,
    gridItemSettings: GridItemSettings,
    wallpaperScroll: Boolean,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
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

    val wallpaperManagerWrapper = LocalWallpaperManager.current

    val view = LocalView.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var lastAppWidgetId by remember { mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID) }

    var deleteAppWidgetId by remember { mutableStateOf(false) }

    var updatedGridItem by remember { mutableStateOf<GridItem?>(null) }

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

    val gridPadding = with(density) {
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
            onUpdateGridItemDataCache = { gridItem ->
                updatedGridItem = gridItem

                onUpdateGridItemDataCache(gridItem)
            },
            onDeleteAppWidgetId = {
                deleteAppWidgetId = true
            },
        )
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleDragIntOffset(
            density = density,
            targetPage = targetPage,
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            gridWidth = gridWidth,
            gridHeight = gridHeight,
            dockHeight = dockHeight,
            gridPadding = gridPadding,
            rows = rows,
            columns = columns,
            dockRows = dockRows,
            dockColumns = dockColumns,
            isScrollInProgress = horizontalPagerState.isScrollInProgress,
            gridItemSource = gridItemSource,
            paddingValues = paddingValues,
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
                    onUpdateGridItemDataCache = { gridItem ->
                        updatedGridItem = gridItem

                        onUpdateGridItemDataCache(gridItem)
                    },
                    onUpdateAppWidgetId = { appWidgetId ->
                        lastAppWidgetId = appWidgetId
                    },
                )
            }

            Drag.Cancel -> {
                onMoveGridItemsFailed(targetPage)
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

    LaunchedEffect(key1 = horizontalPagerState) {
        handleWallpaperScroll(
            horizontalPagerState = horizontalPagerState,
            wallpaperScroll = wallpaperScroll,
            wallpaperManagerWrapper = wallpaperManagerWrapper,
            pageCount = pageCount,
            infiniteScroll = infiniteScroll,
            windowToken = view.windowToken,
        )
    }

    Column(
        modifier = modifier
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            )
            .fillMaxSize(),
    ) {
        HorizontalPager(
            state = horizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                top = horizontalPagerPaddingDp,
                start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr) + horizontalPagerPaddingDp,
                end = paddingValues.calculateRightPadding(LayoutDirection.Ltr) + horizontalPagerPaddingDp,
                bottom = horizontalPagerPaddingDp,
            ),
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
                    DragGridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                    )
                }
            }
        }

        GridLayout(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateRightPadding(LayoutDirection.Ltr),
                )
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
        ) {
            dockGridItems.forEach { gridItem ->
                DragGridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = gridItemSettings,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun DragGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    key(gridItem.id) {
        val currentGridItemSettings = if (gridItem.override) {
            gridItem.gridItemSettings
        } else {
            gridItemSettings
        }

        val currentTextColor = if (gridItem.override) {
            when (gridItem.gridItemSettings.textColor) {
                TextColor.System -> {
                    textColor
                }

                TextColor.Light -> {
                    0xFFFFFFFF
                }

                TextColor.Dark -> {
                    0xFF000000
                }
            }
        } else {
            textColor
        }

        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        showLabel = currentGridItemSettings.showLabel,
                    )
                }

                is GridItemData.Widget -> {
                    WidgetGridItem(modifier = gridItemModifier, data = data)
                }

                is GridItemData.ShortcutInfo -> {
                    ShortcutInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        showLabel = currentGridItemSettings.showLabel,
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        showLabel = currentGridItemSettings.showLabel,
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    textColor: Long,
    showLabel: Boolean,
) {
    Column(
        modifier = modifier
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.label.toString(),
                color = Color(textColor),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Widget,
) {
    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = data.appWidgetId)

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
            modifier = modifier,
        )
    } else {
        AsyncImage(
            model = data.preview,
            contentDescription = null,
            modifier = modifier,
        )
    }
}

@Composable
private fun ShortcutInfoGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    textColor: Long,
    showLabel: Boolean,
) {
    Column(
        modifier = modifier
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = data.icon,
                contentDescription = null,
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                modifier = Modifier.weight(1f),
                text = data.shortLabel,
                color = Color(textColor),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun FolderGridItem(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    textColor: Long,
    showLabel: Boolean,
) {
    Column(
        modifier = modifier
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FlowRow(
            modifier = Modifier.weight(1f),
            maxItemsInEachRow = 2,
        ) {
            data.gridItems.take(6).sortedBy { it.startRow + it.startColumn }.forEach { gridItem ->
                Column {
                    when (val currentData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            AsyncImage(
                                model = currentData.icon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        is GridItemData.Widget -> {
                            Icon(
                                imageVector = EblanLauncherIcons.Widgets,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        is GridItemData.Folder -> {
                            Icon(
                                imageVector = EblanLauncherIcons.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }

        if (showLabel) {
            Text(
                modifier = Modifier.weight(1f),
                text = data.label,
                color = Color(textColor),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}