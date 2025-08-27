package com.eblan.launcher.feature.home.screen.resize

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.FolderGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.component.resize.GridItemResizeOverlay
import com.eblan.launcher.feature.home.component.resize.WidgetGridItemResizeOverlay

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    dockRows: Int,
    dockColumns: Int,
    gridItems: List<GridItem>?,
    gridItem: GridItem?,
    gridWidth: Int,
    gridHeight: Int,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    textColor: Long,
    gridItemSettings: GridItemSettings,
    paddingValues: PaddingValues,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: (GridItem) -> Unit,
    onResizeCancel: () -> Unit,
) {
    requireNotNull(gridItem)

    requireNotNull(gridItems)

    val density = LocalDensity.current

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    val gridPaddingDp = 20.dp

    val gridPaddingPx = with(density) {
        gridPaddingDp.roundToPx()
    }

    BackHandler {
        onResizeCancel()
    }

    Column(
        modifier = modifier
            .padding(paddingValues)
            .fillMaxSize(),
    ) {
        GridLayout(
            modifier = Modifier
                .padding(gridPaddingDp)
                .fillMaxWidth()
                .weight(1f)
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
            gridItems.forEach { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = gridItemSettings,
                )
            }
        }

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
        ) {
            dockGridItems.forEach { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = gridItemSettings,
                )
            }
        }
    }

    when (gridItem.associate) {
        Associate.Grid -> {
            val gridLeft = leftPadding + gridPaddingPx

            val gridTop = topPadding + gridPaddingPx

            val gridWidthWithPadding = gridWidth - (gridPaddingPx * 2)

            val gridHeightWithPadding = (gridHeight - dockHeight) - (gridPaddingPx * 2)

            val cellWidth = gridWidthWithPadding / columns

            val cellHeight = gridHeightWithPadding / rows

            val x = gridItem.startColumn * cellWidth

            val y = gridItem.startRow * cellHeight

            val width = gridItem.columnSpan * cellWidth

            val height = gridItem.rowSpan * cellHeight

            val gridX = x + gridLeft

            val gridY = y + gridTop

            ResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidthWithPadding,
                gridHeight = gridHeightWithPadding,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                rows = rows,
                columns = columns,
                x = gridX,
                y = gridY,
                width = width,
                height = height,
                textColor = textColor,
                onResizeGridItem = onResizeGridItem,
                onResizeEnd = {
                    onResizeEnd(gridItem)
                },
            )
        }

        Associate.Dock -> {
            val cellWidth = gridWidth / dockColumns

            val cellHeight = dockHeight / dockRows

            val x = gridItem.startColumn * cellWidth

            val y = gridItem.startRow * cellHeight

            val dockX = x + leftPadding

            val dockY = (y + topPadding) + (gridHeight - dockHeight)

            val width = gridItem.columnSpan * cellWidth

            val height = gridItem.rowSpan * cellHeight

            ResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidth,
                gridHeight = dockHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                rows = dockRows,
                columns = dockColumns,
                x = dockX,
                y = dockY,
                width = width,
                height = height,
                textColor = textColor,
                onResizeGridItem = onResizeGridItem,
                onResizeEnd = onResizeEnd,
            )
        }
    }
}

@Composable
private fun ResizeOverlay(
    gridItem: GridItem,
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    textColor: Long,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: (GridItem) -> Unit,
) {
    when (val data = gridItem.data) {
        is GridItemData.ApplicationInfo,
        is GridItemData.ShortcutInfo,
        is GridItemData.Folder,
            -> {
            GridItemResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                rows = rows,
                columns = columns,
                x = x,
                y = y,
                width = width,
                height = height,
                color = Color(textColor),
                onResizeGridItem = onResizeGridItem,
                onResizeEnd = onResizeEnd,
            )
        }

        is GridItemData.Widget -> {
            WidgetGridItemResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                rows = rows,
                columns = columns,
                data = data,
                x = x,
                y = y,
                width = width,
                height = height,
                color = Color(textColor),
                onResizeWidgetGridItem = onResizeGridItem,
                onResizeEnd = onResizeEnd,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun GridItemContent(
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
                        gridItemSettings = currentGridItemSettings,
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
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }
            }
        }
    }
}