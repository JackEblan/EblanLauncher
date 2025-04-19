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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.DragGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.GridItemSource
import com.eblan.launcher.feature.home.component.WidgetGridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.calculateTargetPage
import kotlin.math.roundToInt

@Composable
fun DragScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    currentPage: Int,
    userData: UserData,
    dragOffset: Offset,
    gridItemSource: GridItemSource?,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    gridItems: Map<Int, List<GridItem>>,
    drag: Drag,
    preview: ImageBitmap?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
    onUpdateWidget: (
        id: String,
        data: GridItemData,
        appWidgetId: Int,
    ) -> Unit,
    onDeleteGridItem: (GridItem) -> Unit,
    onDragEnd: (Int) -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = userData.infiniteScroll,
        pageCount = userData.pageCount,
    )

    var index by remember { mutableIntStateOf(page) }

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

    val targetPage by remember {
        derivedStateOf {
            calculateTargetPage(
                currentPage = currentPage,
                index = index,
                infiniteScroll = userData.infiniteScroll,
                pageCount = userData.pageCount,
            )
        }
    }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                if (index == 0 && userData.infiniteScroll && newPage && drag == Drag.Dragging) {
                    index = userData.pageCount - 1
                } else if (index == 0 && userData.infiniteScroll && drag == Drag.Dragging) {
                    newPage = true
                } else if (index > 0 && canScroll && drag == Drag.Dragging) {
                    index -= 1
                }
            }

            PageDirection.Right -> {
                if (index == userData.pageCount - 1 && userData.infiniteScroll && newPage && drag == Drag.Dragging) {
                    index = 0
                } else if (index == userData.pageCount - 1 && userData.infiniteScroll && drag == Drag.Dragging) {
                    newPage = true
                } else if (index < userData.pageCount - 1 && canScroll && drag == Drag.Dragging) {
                    index += 1
                }
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = newPage) {
        if (newPage) {
            canScroll = false

            onUpdatePageCount(userData.pageCount + 1)
        }
    }

    LaunchedEffect(key1 = userData.pageCount) {
        if (newPage) {
            index = userData.pageCount - 1

            canScroll = true
        }
    }

    LaunchedEffect(key1 = dragOffset) {
        if (gridItemLayoutInfo != null) {
            onMoveGridItem(
                index,
                gridItemLayoutInfo.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                gridItemLayoutInfo.screenWidth,
                gridItemLayoutInfo.screenHeight,
            )
        }
    }

    LaunchedEffect(key1 = drag) {
        when (gridItemSource) {
            GridItemSource.New -> {
                if (gridItemLayoutInfo != null && drag == Drag.End) {
                    when (val data = gridItemLayoutInfo.gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            onDragEnd(targetPage)
                        }

                        is GridItemData.Widget -> {
                            val allocateAppWidgetId = appWidgetHost.allocateAppWidgetId()

                            val provider = ComponentName.unflattenFromString(data.componentName)

                            if (appWidgetManager.bindAppWidgetIdIfAllowed(
                                    appWidgetId = allocateAppWidgetId,
                                    provider = provider,
                                )
                            ) {
                                onUpdateWidget(
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
                        }
                    }
                }
            }

            GridItemSource.Existing -> {
                if (gridItemLayoutInfo != null && drag == Drag.End) {
                    onDragEnd(targetPage)
                }
            }

            null -> Unit
        }
    }

    LaunchedEffect(key1 = appWidgetId) {
        if (gridItemLayoutInfo != null && appWidgetId > 0) {
            onUpdateWidget(
                gridItemLayoutInfo.gridItem.id,
                gridItemLayoutInfo.gridItem.data,
                appWidgetId,
            )

            onDragEnd(targetPage)
        }

        if (gridItemLayoutInfo != null && appWidgetId < 0) {
            onDeleteGridItem(gridItemLayoutInfo.gridItem)

            onDragEnd(targetPage)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray),
    ) {
        if (gridItemLayoutInfo != null) {
            GridItemOverlay(
                preview = preview,
                gridItemLayoutInfo = gridItemLayoutInfo,
                dragOffset = dragOffset.round(),
            )
        }

        AnimatedContent(
            targetState = index,
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
                rows = userData.rows,
                columns = userData.columns,
                gridItems = gridItems,
                gridItemContent = { gridItem ->
                    when (val gridItemData = gridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(gridItemData = gridItemData)
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(gridItemData = gridItemData)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun GridItemOverlay(
    modifier: Modifier = Modifier,
    preview: ImageBitmap?,
    gridItemLayoutInfo: GridItemLayoutInfo,
    dragOffset: IntOffset,
) {
    val density = LocalDensity.current

    val size = with(density) {
        DpSize(
            width = gridItemLayoutInfo.width.toDp(),
            height = gridItemLayoutInfo.height.toDp(),
        )
    }

    if (preview != null) {
        Image(
            bitmap = preview,
            modifier = modifier
                .offset {
                    dragOffset
                }
                .size(size)
                .alpha(0.5f)
                .zIndex(1f),
            contentDescription = null,
        )
    }
}