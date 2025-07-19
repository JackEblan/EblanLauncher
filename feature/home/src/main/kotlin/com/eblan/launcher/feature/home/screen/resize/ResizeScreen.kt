package com.eblan.launcher.feature.home.screen.resize

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.resize.GridItemResizeOverlay
import com.eblan.launcher.feature.home.component.resize.WidgetGridItemResizeOverlay
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo

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

    val color = when (textColor) {
        TextColor.White -> Color.White
        TextColor.Black -> Color.Black
    }

    val gridPaddingDp = 20.dp

    val gridPaddingPx = with(density) {
        gridPaddingDp.roundToPx()
    }

    Column(modifier = modifier.fillMaxSize()) {
        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(gridPaddingDp)
                .weight(1f)
                .border(
                    width = 2.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                )
                .background(color = Color.White.copy(alpha = 0.25f)),
            rows = rows,
            columns = columns,
        ) {
            gridItems?.forEach { gridItem ->
                key(gridItem.id) {
                    LookaheadScope {
                        val gridItemModifier = Modifier
                            .animateBounds(this)
                            .gridItem(gridItem)

                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                ApplicationInfoGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                    color = color,
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                )
                            }

                            is GridItemData.ShortcutInfo -> {
                                ShortcutInfoGridItem(
                                    modifier = gridItemModifier,
                                    data = data,
                                    color = color,
                                )
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
                    when (val data = dockGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            ApplicationInfoGridItem(
                                modifier = Modifier
                                    .gridItem(dockGridItem)
                                    .fillMaxSize(),
                                data = data,
                                color = color,
                            )
                        }

                        is GridItemData.Widget -> {
                            WidgetGridItem(
                                modifier = Modifier
                                    .gridItem(dockGridItem)
                                    .fillMaxSize(),
                                data = data,
                            )
                        }

                        is GridItemData.ShortcutInfo -> {
                            ShortcutInfoGridItem(
                                modifier = Modifier
                                    .gridItem(dockGridItem)
                                    .fillMaxSize(),
                                data = data,
                                color = color,
                            )
                        }
                    }
                }
            }
        }
    }

    if (gridItemLayoutInfo != null) {
        val gridWidth = rootWidth - (gridPaddingPx * 2)

        val gridHeight = (rootHeight - dockHeight) - (gridPaddingPx * 2)

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        when (val data = gridItemLayoutInfo.gridItem.data) {
            is GridItemData.ApplicationInfo, is GridItemData.ShortcutInfo -> {
                GridItemResizeOverlay(
                    gridPadding = gridPaddingPx,
                    gridItems = gridItems,
                    gridItem = gridItemLayoutInfo.gridItem,
                    gridWidth = gridWidth,
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
                    gridPadding = gridPaddingPx,
                    gridItems = gridItems,
                    gridItem = gridItemLayoutInfo.gridItem,
                    gridWidth = gridWidth,
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