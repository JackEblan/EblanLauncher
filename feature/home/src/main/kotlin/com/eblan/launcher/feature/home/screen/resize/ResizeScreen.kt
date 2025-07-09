package com.eblan.launcher.feature.home.screen.resize

import android.widget.FrameLayout
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.local.LocalAppWidgetHost
import com.eblan.launcher.designsystem.local.LocalAppWidgetManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.resize.GridItemResizeOverlay
import com.eblan.launcher.feature.home.component.resize.WidgetGridItemResizeOverlay

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    dockRows: Int,
    dockColumns: Int,
    gridItems: List<GridItem>?,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    rootWidth: Int,
    rootHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: TextColor,
    onResizeGridItem: (
        gridItems: List<GridItem>,
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val appWidgetManager = LocalAppWidgetManager.current

    val appWidgetHost = LocalAppWidgetHost.current

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            rows = rows,
            columns = columns,
        ) {
            gridItems?.forEach { gridItem ->
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
                    when (val gridItemData = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            Column(
                                modifier = Modifier
                                    .gridItem(dockGridItem)
                                    .fillMaxSize(),
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
                                    modifier = Modifier
                                        .gridItem(dockGridItem)
                                        .fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (gridItemLayoutInfo != null) {
        val gridHeight = rootHeight - dockHeight

        val cellWidth = rootWidth / columns

        val cellHeight = gridHeight / rows

        when (val data = gridItemLayoutInfo.gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                GridItemResizeOverlay(
                    gridItems = gridItems,
                    gridItem = gridItemLayoutInfo.gridItem,
                    gridWidth = rootWidth,
                    gridHeight = gridHeight,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    rows = rows,
                    columns = columns,
                    startRow = gridItemLayoutInfo.gridItem.startRow,
                    startColumn = gridItemLayoutInfo.gridItem.startColumn,
                    rowSpan = gridItemLayoutInfo.gridItem.rowSpan,
                    columnSpan = gridItemLayoutInfo.gridItem.columnSpan,
                    onResizeGridItem = onResizeGridItem,
                    onResizeEnd = onResizeEnd,
                )
            }

            is GridItemData.Widget -> {
                WidgetGridItemResizeOverlay(
                    gridItems = gridItems,
                    gridItem = gridItemLayoutInfo.gridItem,
                    gridWidth = rootWidth,
                    gridHeight = gridHeight,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    rows = rows,
                    columns = columns,
                    data = data,
                    startRow = gridItemLayoutInfo.gridItem.startRow,
                    startColumn = gridItemLayoutInfo.gridItem.startColumn,
                    rowSpan = gridItemLayoutInfo.gridItem.rowSpan,
                    columnSpan = gridItemLayoutInfo.gridItem.columnSpan,
                    onResizeWidgetGridItem = onResizeGridItem,
                    onResizeEnd = onResizeEnd,
                )
            }
        }
    }
}