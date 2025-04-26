package com.eblan.launcher.feature.home.screen.resize

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.Dock
import com.eblan.launcher.feature.home.component.ResizeGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItem
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    rows: Int,
    columns: Int,
    pageCount: Int,
    infiniteScroll: Boolean,
    dockRows: Int,
    dockColumns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    dockHeight: Int,
    dockGridItems: List<GridItem>,
    onResizeGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        dockHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        dockHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    val density = LocalDensity.current

    val dockHeightDp = with(density) {
        dockHeight.toDp()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        ResizeGridSubcomposeLayout(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f),
            page = page,
            rows = rows,
            columns = columns,
            lastGridItemLayoutInfo = gridItemLayoutInfo,
            gridItems = gridItems,
            onResizeGridItem = { gridItem, width, height, gridWidth, gridHeight, anchor ->
                onResizeGridItem(
                    gridItem,
                    width,
                    height,
                    gridWidth,
                    gridHeight,
                    dockHeight,
                    anchor,
                )
            },
            onResizeWidgetGridItem = { gridItem, width, height, gridWidth, gridHeight, anchor ->
                onResizeWidgetGridItem(
                    gridItem,
                    width,
                    height,
                    gridWidth,
                    gridHeight,
                    dockHeight,
                    anchor,
                )
            },
            onResizeEnd = onResizeEnd,
            gridItemContent = { gridItem ->
                when (val gridItemData = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        ApplicationInfoGridItem(
                            gridItemData = gridItemData,
                        )
                    }

                    is GridItemData.Widget -> {
                        WidgetGridItem(
                            gridItemData = gridItemData,
                        )
                    }
                }
            },
        )

        Dock(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            rows = dockRows,
            columns = dockColumns,
            dockGridItems = dockGridItems,
        ) { dockItem, x, y, width, height ->
            when (val gridItemData = dockItem.data) {
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