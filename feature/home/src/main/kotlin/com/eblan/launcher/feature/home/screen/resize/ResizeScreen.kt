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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.util.PAGE_INDICATOR_HEIGHT
import com.eblan.launcher.feature.home.util.getSystemTextColor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedTransitionScope.ResizeScreen(
    modifier: Modifier = Modifier,
    dockCurrentPage: Int,
    gridCurrentPage: Int,
    gridHorizontalPagerState: PagerState,
    gridItem: GridItem?,
    gridItemCache: GridItemCache,
    hasShortcutHostPermission: Boolean,
    homeSettings: HomeSettings,
    iconPackFilePaths: Map<String, String>,
    lockMovement: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    paddingValues: PaddingValues,
    screen: Screen,
    screenHeight: Int,
    screenWidth: Int,
    statusBarNotifications: Map<String, Int>,
    textColor: TextColor,
    onResizeCancel: () -> Unit,
    onResizeEnd: (GridItem) -> Unit,
    onResizeGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
        lockMovement: Boolean,
    ) -> Unit
) {
    requireNotNull(gridItem)

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

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val dockHeight = homeSettings.dockHeight.dp

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    val pageIndicatorHeightPx = with(density) {
        PAGE_INDICATOR_HEIGHT.roundToPx()
    }

    var currentGridItem by remember {
        mutableStateOf(gridItem)
    }

    LaunchedEffect(key1 = moveGridItemResult) {
        moveGridItemResult?.movingGridItem?.let { movingGridItem ->
            currentGridItem = movingGridItem
        }
    }

    BackHandler {
        onResizeCancel()
    }

    Column(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onTap = {
                        onResizeEnd(currentGridItem)
                    },
                )
            }
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        GridLayout(
            modifier = Modifier.weight(1f),
            gridItems = gridItemCache.gridItemsCacheByPage[gridCurrentPage].orEmpty(),
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = homeSettings.gridItemSettings,
                    isDragging = false,
                    statusBarNotifications = statusBarNotifications,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = Drag.End,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = screen,
                    isScrollInProgress = false,
                )
            },
        )

        PageIndicator(
            modifier = Modifier
                .height(PAGE_INDICATOR_HEIGHT)
                .fillMaxWidth(),
            gridHorizontalPagerState = gridHorizontalPagerState,
            infiniteScroll = homeSettings.infiniteScroll,
            pageCount = homeSettings.pageCount,
            color = getSystemTextColor(
                systemCustomTextColor = homeSettings.gridItemSettings.customTextColor,
                systemTextColor = textColor
            ),
        )

        GridLayout(
            modifier = Modifier
                .fillMaxWidth()
                .height(dockHeight),
            gridItems = gridItemCache.dockGridItemsCache[dockCurrentPage],
            columns = homeSettings.dockColumns,
            rows = homeSettings.dockRows,
            { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSettings = homeSettings.gridItemSettings,
                    isDragging = false,
                    statusBarNotifications = statusBarNotifications,
                    hasShortcutHostPermission = hasShortcutHostPermission,
                    drag = Drag.End,
                    iconPackFilePaths = iconPackFilePaths,
                    screen = screen,
                    isScrollInProgress = false,
                )
            },
        )
    }

    when (currentGridItem.associate) {
        Associate.Grid -> {
            val gridHeight = safeDrawingHeight - pageIndicatorHeightPx - dockHeightPx

            val cellWidth = safeDrawingWidth / homeSettings.columns

            val cellHeight = gridHeight / homeSettings.rows

            val x = currentGridItem.startColumn * cellWidth

            val y = currentGridItem.startRow * cellHeight

            val width = currentGridItem.columnSpan * cellWidth

            val height = currentGridItem.rowSpan * cellHeight

            val gridX = x + leftPadding

            val gridY = y + topPadding

            ResizeOverlay(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                columns = homeSettings.columns,
                gridHeight = gridHeight,
                gridItem = currentGridItem,
                gridItemSettings = homeSettings.gridItemSettings,
                gridWidth = safeDrawingWidth,
                height = height,
                lockMovement = lockMovement,
                rows = homeSettings.rows,
                textColor = textColor,
                width = width,
                x = gridX,
                y = gridY,
                onResizeGridItem = onResizeGridItem
            )
        }

        Associate.Dock -> {
            val cellWidth = safeDrawingWidth / homeSettings.dockColumns

            val cellHeight = dockHeightPx / homeSettings.dockRows

            val x = currentGridItem.startColumn * cellWidth

            val y = currentGridItem.startRow * cellHeight

            val dockX = x + leftPadding

            val dockY = (y + topPadding) + (safeDrawingHeight - dockHeightPx)

            val width = currentGridItem.columnSpan * cellWidth

            val height = currentGridItem.rowSpan * cellHeight

            ResizeOverlay(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                columns = homeSettings.dockColumns,
                gridHeight = dockHeightPx,
                gridItem = currentGridItem,
                gridItemSettings = homeSettings.gridItemSettings,
                gridWidth = safeDrawingWidth,
                height = height,
                lockMovement = lockMovement,
                rows = homeSettings.dockRows,
                textColor = textColor,
                width = width,
                x = dockX,
                y = dockY,
                onResizeGridItem = onResizeGridItem
            )
        }
    }
}
