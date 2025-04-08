package com.eblan.launcher.feature.home.screen.placeholder

import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.eblan.launcher.feature.home.model.GridItemByCoordinates
import com.eblan.launcher.feature.home.screen.placeholder.component.GridSubcomposeLayout
import kotlin.math.roundToInt

@Composable
fun PlaceholderScreen(
    modifier: Modifier = Modifier,
    pageDirection: PageDirection?,
    page: Int,
    rows: Int,
    columns: Int,
    dragOffset: Offset,
    lastGridItemByCoordinates: GridItemByCoordinates?,
    gridItems: Map<Int, List<GridItem>>,
    onMoveGridItem: (
        page: Int,
        gridItem: GridItem,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
) {
    var index by remember { mutableIntStateOf(page) }

    LaunchedEffect(key1 = pageDirection) {
        when (pageDirection) {
            PageDirection.Left -> {
                index -= 1
            }

            PageDirection.Right -> {
                index += 1
            }

            null -> Unit
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

    GridSubcomposeLayout(
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