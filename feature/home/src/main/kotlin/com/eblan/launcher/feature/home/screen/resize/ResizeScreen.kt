/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.screen.resize

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.resize.GridItemResizeOverlay
import com.eblan.launcher.feature.home.component.resize.WidgetGridItemResizeOverlay
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor

@Composable
fun ResizeScreen(
    modifier: Modifier = Modifier,
    gridItemsCacheByPage: List<GridItem>?,
    gridItem: GridItem?,
    screenWidth: Int,
    screenHeight: Int,
    dockGridItemsCache: List<GridItem>,
    textColor: TextColor,
    paddingValues: PaddingValues,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: (GridItem) -> Unit,
    onResizeCancel: () -> Unit,
) {
    requireNotNull(gridItem)

    requireNotNull(gridItemsCacheByPage)

    val density = LocalDensity.current

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val dockHeightDp = with(density) {
        homeSettings.dockHeight.toDp()
    }

    BackHandler {
        onResizeCancel()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            gridItems = gridItemsCacheByPage,
            rows = homeSettings.rows,
            columns = homeSettings.columns,
        ) { gridItem ->
            GridItemContent(
                gridItem = gridItem,
                textColor = textColor,
                gridItemSettings = homeSettings.gridItemSettings,
                iconPackInfoPackageName = iconPackInfoPackageName,
                isDragging = false,
                hasShortcutHostPermission = hasShortcutHostPermission,
            )
        }

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeightDp),
            gridItems = dockGridItemsCache,
            rows = homeSettings.dockRows,
            columns = homeSettings.dockColumns,
        ) { gridItem ->
            GridItemContent(
                gridItem = gridItem,
                textColor = textColor,
                gridItemSettings = homeSettings.gridItemSettings,
                iconPackInfoPackageName = iconPackInfoPackageName,
                isDragging = false,
                hasShortcutHostPermission = hasShortcutHostPermission,
            )
        }
    }

    when (gridItem.associate) {
        Associate.Grid -> {
            val cellWidth = gridWidth / homeSettings.columns

            val cellHeight = (gridHeight - homeSettings.dockHeight) / homeSettings.rows

            val x = gridItem.startColumn * cellWidth

            val y = gridItem.startRow * cellHeight

            val width = gridItem.columnSpan * cellWidth

            val height = gridItem.rowSpan * cellHeight

            val gridX = x + leftPadding

            val gridY = y + topPadding

            ResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidth,
                gridHeight = gridHeight - homeSettings.dockHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                rows = homeSettings.rows,
                columns = homeSettings.columns,
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
            val cellWidth = gridWidth / homeSettings.dockColumns

            val cellHeight = homeSettings.dockHeight / homeSettings.dockRows

            val x = gridItem.startColumn * cellWidth

            val y = gridItem.startRow * cellHeight

            val dockX = x + leftPadding

            val dockY = (y + topPadding) + (gridHeight - homeSettings.dockHeight)

            val width = gridItem.columnSpan * cellWidth

            val height = gridItem.rowSpan * cellHeight

            ResizeOverlay(
                gridItem = gridItem,
                gridWidth = gridWidth,
                gridHeight = homeSettings.dockHeight,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                rows = homeSettings.dockRows,
                columns = homeSettings.dockColumns,
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
    textColor: TextColor,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: (GridItem) -> Unit,
) {
    val currentTextColor = if (gridItem.override) {
        getGridItemTextColor(
            systemTextColor = textColor,
            gridItemTextColor = gridItem.gridItemSettings.textColor,
        )
    } else {
        getSystemTextColor(textColor = textColor)
    }

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
                color = currentTextColor,
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
                color = currentTextColor,
                onResizeWidgetGridItem = onResizeGridItem,
                onResizeEnd = onResizeEnd,
            )
        }
    }
}
