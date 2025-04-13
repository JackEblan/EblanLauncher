package com.eblan.launcher.feature.home.screen.grid

import android.widget.FrameLayout
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.model.DragType
import com.eblan.launcher.feature.home.screen.grid.component.SimpleGridSubcomposeLayout
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.calculateTargetPage
import kotlin.math.roundToInt

@Composable
fun GridScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    currentPage: Int,
    userData: UserData,
    dragOffset: Offset,
    lastGridItemDimensions: GridItemDimensions?,
    gridCacheItems: Map<Int, List<GridItem>>,
    dragType: DragType,
    addGridItemDimensions: GridItemDimensions?,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    onUpdatePageCount: (Int) -> Unit,
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

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                if (index == 0 &&
                    userData.infiniteScroll &&
                    newPage &&
                    dragType == DragType.Drag
                ) {
                    index = userData.pageCount - 1
                } else if (index == 0 &&
                    userData.infiniteScroll &&
                    dragType == DragType.Drag
                ) {
                    newPage = true
                } else if (index > 0 &&
                    canScroll &&
                    dragType == DragType.Drag
                ) {
                    index -= 1
                }
            }

            PageDirection.Right -> {
                if (index == userData.pageCount - 1 &&
                    userData.infiniteScroll &&
                    newPage &&
                    dragType == DragType.Drag
                ) {
                    index = 0
                } else if (index == userData.pageCount - 1 &&
                    userData.infiniteScroll &&
                    dragType == DragType.Drag
                ) {
                    newPage = true
                } else if (index < userData.pageCount - 1 &&
                    canScroll &&
                    dragType == DragType.Drag
                ) {
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
        if (lastGridItemDimensions != null) {
            onMoveGridItem(
                index,
                lastGridItemDimensions.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                lastGridItemDimensions.screenWidth,
                lastGridItemDimensions.screenHeight,
            )
        }

        if (addGridItemDimensions != null) {
            onMoveGridItem(
                index,
                addGridItemDimensions.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                addGridItemDimensions.screenWidth,
                addGridItemDimensions.screenHeight,
            )
        }
    }

    LaunchedEffect(key1 = dragType) {
        if (dragType == DragType.End || dragType == DragType.Cancel) {
            val targetPage = calculateTargetPage(
                currentPage = currentPage,
                index = index,
                infiniteScroll = userData.infiniteScroll,
                pageCount = userData.pageCount,
            )

            onDragEnd(targetPage)
        }
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
        label = "animated content",
    ) { targetCount ->
        SimpleGridSubcomposeLayout(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Gray),
            index = targetCount,
            rows = userData.rows,
            columns = userData.columns,
            gridItems = gridCacheItems,
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

@Composable
private fun ApplicationInfoGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.ApplicationInfo,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue),
    ) {
        AsyncImage(model = gridItemData.icon, contentDescription = null)

        Text(text = gridItemData.label)
    }
}

@Composable
private fun WidgetGridItem(
    modifier: Modifier = Modifier,
    gridItemData: GridItemData.Widget,
) {
    val appWidgetHost = LocalAppWidgetHost.current

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId = gridItemData.appWidgetId)

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
            modifier = modifier,
        )
    }
}