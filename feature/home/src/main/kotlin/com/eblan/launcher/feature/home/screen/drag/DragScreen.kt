package com.eblan.launcher.feature.home.screen.drag

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Intent
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.eblan.launcher.feature.home.screen.pager.GridItemMenu
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.calculateTargetPage

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
    gridItemOffset: IntOffset,
    gridItemSource: GridItemSource?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    constraintsMaxWidth: Int,
    constraintsMaxHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
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
    onDragEnd: (Int) -> Unit,
    onEdit: () -> Unit,
    onResize: () -> Unit,
) {
    val startingPage = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    var index by remember { mutableIntStateOf(startingPage) }

    var newPage by remember { mutableStateOf(false) }

    var canScroll by remember { mutableStateOf(true) }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    var showMenu by remember { mutableStateOf(false) }

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

                val targetPage = calculateTargetPage(
                    currentPage = currentPage,
                    index = index,
                    infiniteScroll = infiniteScroll,
                    pageCount = pageCount,
                )

                onDragEnd(targetPage)
            }
        } else {
            appWidgetHost.deleteAppWidgetId(appWidgetId = appWidgetId)

            if (gridItemSource?.gridItemLayoutInfo != null && appWidgetId < 0) {
                onDeleteGridItem(gridItemSource.gridItemLayoutInfo.gridItem)

                val targetPage = calculateTargetPage(
                    currentPage = currentPage,
                    index = index,
                    infiniteScroll = infiniteScroll,
                    pageCount = pageCount,
                )

                onDragEnd(targetPage)
            }
        }
    }

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
        val isDraggingOnDock = gridItemOffset.y > constraintsMaxHeight - dockHeight

        if (drag == Drag.Dragging && gridItemSource?.gridItemLayoutInfo != null) {
            if ((gridItemOffset.x - gridItemSource.gridItemLayoutInfo.width / 2) < 0 && !isDraggingOnDock) {
                pageDirection = PageDirection.Left
            } else if ((gridItemOffset.x + gridItemSource.gridItemLayoutInfo.width / 2) > constraintsMaxWidth && !isDraggingOnDock) {
                pageDirection = PageDirection.Right
            } else {
                pageDirection = null

                if (isDraggingOnDock) {
                    val cellWidth = constraintsMaxWidth / dockColumns

                    val cellHeight = dockHeight / dockRows

                    val dockY = gridItemOffset.y - (constraintsMaxHeight - dockHeight)

                    val gridItem = gridItemSource.gridItemLayoutInfo.gridItem.copy(
                        page = index,
                        startRow = dockY / cellHeight,
                        startColumn = gridItemOffset.x / cellWidth,
                        associate = Associate.Dock,
                    )

                    onMoveGridItem(gridItem, dockRows, dockColumns)
                } else {
                    val gridHeight = constraintsMaxHeight - dockHeight

                    val cellWidth = constraintsMaxWidth / columns

                    val cellHeight = gridHeight / rows

                    val gridItem = gridItemSource.gridItemLayoutInfo.gridItem.copy(
                        page = index,
                        startRow = gridItemOffset.y / cellHeight,
                        startColumn = gridItemOffset.x / cellWidth,
                        associate = Associate.Grid,
                    )

                    onMoveGridItem(gridItem, rows, columns)
                }
            }
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
                                    val targetPage = calculateTargetPage(
                                        currentPage = currentPage,
                                        index = index,
                                        infiniteScroll = infiniteScroll,
                                        pageCount = pageCount,
                                    )

                                    onDragEnd(targetPage)
                                }

                                is GridItemData.Widget -> {
                                    val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

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

                                        val targetPage = calculateTargetPage(
                                            currentPage = currentPage,
                                            index = index,
                                            infiniteScroll = infiniteScroll,
                                            pageCount = pageCount,
                                        )

                                        onDragEnd(targetPage)
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
                                }
                            }
                        }

                        GridItemSource.Type.Old -> {
                            val targetPage = calculateTargetPage(
                                currentPage = currentPage,
                                index = index,
                                infiniteScroll = infiniteScroll,
                                pageCount = pageCount,
                            )

                            onDragEnd(targetPage)
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxSize(),
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
                                    val color = when (textColor) {
                                        TextColor.White -> Color.White
                                        TextColor.Black -> Color.Black
                                    }

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
                            val color = when (textColor) {
                                TextColor.White -> Color.White
                                TextColor.Black -> Color.Black
                            }

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
                        x = gridItemOffset.x - gridItemSource.gridItemLayoutInfo.width / 2,
                        y = gridItemOffset.y - gridItemSource.gridItemLayoutInfo.height / 2,
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
                        y = constraintsMaxHeight - dockHeight,
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