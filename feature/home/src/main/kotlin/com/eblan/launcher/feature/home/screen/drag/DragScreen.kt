package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.DockGrid
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.calculateTargetPage
import kotlin.math.roundToInt

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
    gridItemOffset: Offset,
    gridItemSource: GridItemSource?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    preview: ImageBitmap?,
    constraintsMaxWidth: Int,
    constraintsMaxHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    onMoveGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onUpdateWidgetGridItem: (
        id: String,
        data: GridItemData,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragCancel: () -> Unit,
    onDragEnd: (
        targetPage: Int,
        associate: Associate,
    ) -> Unit,
) {
    val startingPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var index by remember { mutableIntStateOf(startingPage) }

    var newPage by remember { mutableStateOf(false) }

    var canScroll by remember { mutableStateOf(true) }

    var appWidgetId by remember { mutableIntStateOf(0) }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        appWidgetId = if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        } else {
            -1
        }
    }

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                if (index == 0 && infiniteScroll && newPage && drag == Drag.Dragging) {
                    index = pageCount - 1
                } else if (index == 0 && infiniteScroll && drag == Drag.Dragging) {
                    newPage = true
                } else if (index > 0 && canScroll && drag == Drag.Dragging) {
                    index -= 1
                }
            }

            PageDirection.Right -> {
                if (index == pageCount - 1 && infiniteScroll && newPage && drag == Drag.Dragging) {
                    index = 0
                } else if (index == pageCount - 1 && infiniteScroll && drag == Drag.Dragging) {
                    newPage = true
                } else if (index == pageCount - 1 && drag == Drag.Dragging) {
                    newPage = true
                } else if (index < pageCount - 1 && canScroll && drag == Drag.Dragging) {
                    index += 1
                }
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = newPage) {
        if (newPage) {
            canScroll = false

            onUpdatePageCount(pageCount + 1)
        }
    }

    LaunchedEffect(key1 = pageCount) {
        if (newPage) {
            index = pageCount - 1

            canScroll = true
        }
    }

    LaunchedEffect(key1 = gridItemOffset) {
        if (drag == Drag.Dragging && gridItemSource?.gridItemLayoutInfo != null) {
            if ((gridItemOffset.x + gridItemSource.gridItemLayoutInfo.width * 0.25) < 0) {
                pageDirection = PageDirection.Left
            } else if ((gridItemOffset.x + gridItemSource.gridItemLayoutInfo.width * 0.75) > constraintsMaxWidth) {
                pageDirection = PageDirection.Right
            } else {
                pageDirection = null

                if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                    val dockY = gridItemOffset.y.roundToInt() - (constraintsMaxHeight - dockHeight)

                    val gridItem = moveGridItemWithCoordinates(
                        gridItem = gridItemSource.gridItemLayoutInfo.gridItem,
                        x = gridItemOffset.x.roundToInt(),
                        y = dockY,
                        rows = dockRows,
                        columns = dockColumns,
                        gridWidth = constraintsMaxWidth,
                        gridHeight = dockHeight,
                    ).copy(
                        page = index,
                        associate = Associate.Dock,
                    )

                    onMoveGridItem(gridItem, dockRows, dockColumns)
                } else {
                    val gridItem = moveGridItemWithCoordinates(
                        gridItem = gridItemSource.gridItemLayoutInfo.gridItem,
                        x = gridItemOffset.x.roundToInt(),
                        y = gridItemOffset.y.roundToInt(),
                        rows = rows,
                        columns = columns,
                        gridWidth = constraintsMaxWidth,
                        gridHeight = constraintsMaxHeight,
                    ).copy(
                        page = index,
                        associate = Associate.Grid,
                    )

                    onMoveGridItem(gridItem, rows, columns)
                }
            }
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.Cancel) {
            onDragCancel()
        }

        when (gridItemSource?.type) {
            GridItemSource.Type.New -> {
                if (drag == Drag.End) {
                    when (val data = gridItemSource.gridItemLayoutInfo.gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            val targetPage = calculateTargetPage(
                                currentPage = currentPage,
                                index = index,
                                infiniteScroll = infiniteScroll,
                                pageCount = pageCount,
                            )

                            val associate =
                                if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                                    Associate.Dock
                                } else {
                                    Associate.Grid
                                }

                            onDragEnd(targetPage, associate)
                        }

                        is GridItemData.Widget -> {
                            val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

                            val provider = ComponentName.unflattenFromString(data.componentName)

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

                                val targetPage = calculateTargetPage(
                                    currentPage = currentPage,
                                    index = index,
                                    infiniteScroll = infiniteScroll,
                                    pageCount = pageCount,
                                )

                                val associate =
                                    if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                                        Associate.Dock
                                    } else {
                                        Associate.Grid
                                    }

                                onDragEnd(targetPage, associate)
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
                        }
                    }
                }
            }

            GridItemSource.Type.Old -> {
                if (drag == Drag.End) {
                    val targetPage = calculateTargetPage(
                        currentPage = currentPage,
                        index = index,
                        infiniteScroll = infiniteScroll,
                        pageCount = pageCount,
                    )

                    val associate =
                        if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                            Associate.Dock
                        } else {
                            Associate.Grid
                        }

                    onDragEnd(targetPage, associate)
                }
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = appWidgetId) {
        if (gridItemSource?.gridItemLayoutInfo != null && appWidgetId > 0) {
            onUpdateWidgetGridItem(
                gridItemSource.gridItemLayoutInfo.gridItem.id,
                gridItemSource.gridItemLayoutInfo.gridItem.data,
                appWidgetId,
            )

            val targetPage = calculateTargetPage(
                currentPage = currentPage,
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            val associate =
                if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                    Associate.Dock
                } else {
                    Associate.Grid
                }

            onDragEnd(targetPage, associate)
        }

        if (gridItemSource?.gridItemLayoutInfo != null && appWidgetId < 0) {
            onDeleteGridItem(gridItemSource.gridItemLayoutInfo.gridItem)

            val targetPage = calculateTargetPage(
                currentPage = currentPage,
                index = index,
                infiniteScroll = infiniteScroll,
                pageCount = pageCount,
            )

            val associate =
                if (gridItemOffset.y > constraintsMaxHeight - dockHeight) {
                    Associate.Dock
                } else {
                    Associate.Grid
                }

            onDragEnd(targetPage, associate)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (gridItemSource?.gridItemLayoutInfo != null) {
            GridItemOverlay(
                preview = preview,
                gridItemLayoutInfo = gridItemSource.gridItemLayoutInfo,
                offset = gridItemOffset.round(),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = index,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
                    modifier = Modifier
                        .fillMaxSize(),
                    index = targetCount,
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
                                    ApplicationInfoGridItem(gridItemData = gridItemData)
                                }

                                is GridItemData.Widget -> {
                                    WidgetGridItem(gridItemData = gridItemData)
                                }
                            }
                        }
                    },
                )
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
                            ApplicationInfoGridItem(gridItemData = gridItemData)
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(gridItemData = gridItemData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    preview: ImageBitmap?,
    gridItemLayoutInfo: GridItemLayoutInfo,
    offset: IntOffset,
) {
    val density = LocalDensity.current

    val size = remember {
        with(density) {
            DpSize(
                width = gridItemLayoutInfo.width.toDp(),
                height = gridItemLayoutInfo.height.toDp(),
            )
        }
    }
    if (preview != null) {
        Surface(
            modifier = modifier
                .offset {
                    offset
                }
                .size(size)
                .zIndex(1f),
        ) {
            Image(
                bitmap = preview,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}