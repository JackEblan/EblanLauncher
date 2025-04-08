package com.eblan.launcher.feature.home.screen.grid

import android.widget.FrameLayout
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
import com.eblan.launcher.domain.model.PageDirection
import com.eblan.launcher.feature.home.model.DragType
import com.eblan.launcher.feature.home.model.GridItemByCoordinates
import kotlin.math.roundToInt

@Composable
fun GridScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    dragOffset: Offset,
    lastGridItemByCoordinates: GridItemByCoordinates?,
    gridItems: Map<Int, List<GridItem>>,
    dragType: DragType,
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
    val z = currentPage - (Int.MAX_VALUE / 2)

    val page = z - z.floorDiv(pageCount) * pageCount

    var index by remember { mutableIntStateOf(page) }

    var newPage by remember { mutableStateOf(false) }

    var canScroll by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                if (index == 0) {
                    if (newPage) {
                        index = pageCount - 1
                    }

                    newPage = true
                } else if (canScroll) {
                    index -= 1
                }
            }

            PageDirection.Right -> {
                if (index == pageCount - 1) {
                    if (newPage) {
                        index = 0
                    }

                    newPage = true
                } else if (canScroll) {
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

    LaunchedEffect(key1 = dragOffset) {
        if (lastGridItemByCoordinates != null) {
            onMoveGridItem(
                index,
                lastGridItemByCoordinates.gridItem,
                dragOffset.x.roundToInt(),
                dragOffset.y.roundToInt(),
                lastGridItemByCoordinates.screenWidth,
                lastGridItemByCoordinates.screenHeight,
            )
        }
    }

    LaunchedEffect(key1 = dragType) {
        if (dragType == DragType.End || dragType == DragType.Cancel) {
            val targetPage = run {
                val offset = currentPage - (Int.MAX_VALUE / 2)
                val currentReal = offset - Math.floorDiv(
                    offset,
                    pageCount,
                ) * pageCount
                val delta = index - currentReal
                currentPage + delta
            }

            onDragEnd(targetPage)
        }
    }

    SimpleGridSubcomposeLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray),
        index = index,
        rows = rows,
        columns = columns,
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