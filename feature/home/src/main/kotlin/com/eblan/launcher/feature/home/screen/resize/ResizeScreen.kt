package com.eblan.launcher.feature.home.screen.resize

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
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
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
    constraintMaxWidth: Int,
    constraintMaxHeight: Int,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        page: Int,
        gridItem: GridItem,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val page = calculatePage(
        index = currentPage,
        infiniteScroll = infiniteScroll,
        pageCount = pageCount,
    )

    ResizeGridSubcomposeLayout(
        modifier = modifier
            .fillMaxSize(),
        page = page,
        rows = rows,
        columns = columns,
        lastGridItemLayoutInfo = gridItemLayoutInfo,
        gridItems = gridItems,
        constraintMaxWidth = constraintMaxWidth,
        constraintMaxHeight = constraintMaxHeight,
        onResizeGridItem = onResizeGridItem,
        onResizeWidgetGridItem = onResizeWidgetGridItem,
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
}