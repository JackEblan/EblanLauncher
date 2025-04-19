package com.eblan.launcher.feature.home.screen.resize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.GridItemLayoutInfo
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.feature.home.component.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.ResizeGridSubcomposeLayout
import com.eblan.launcher.feature.home.component.WidgetGridItem
import com.eblan.launcher.feature.home.util.calculatePage

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    currentPage: Int,
    userData: UserData,
    gridItems: Map<Int, List<GridItem>>,
    gridItemLayoutInfo: GridItemLayoutInfo?,
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
        infiniteScroll = userData.infiniteScroll,
        pageCount = userData.pageCount,
    )

    ResizeGridSubcomposeLayout(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray),
        page = page,
        rows = userData.rows,
        columns = userData.columns,
        lastGridItemLayoutInfo = gridItemLayoutInfo,
        gridItems = gridItems,
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