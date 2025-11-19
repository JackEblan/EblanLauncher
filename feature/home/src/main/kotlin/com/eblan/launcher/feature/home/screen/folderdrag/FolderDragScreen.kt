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
package com.eblan.launcher.feature.home.screen.folderdrag

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.indicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.screen.drag.handlePageDirection
import com.eblan.launcher.feature.home.util.getGridItemTextColor
import com.eblan.launcher.feature.home.util.getSystemTextColor

@Composable
internal fun FolderDragScreen(
    modifier: Modifier = Modifier,
    gridItemCache: GridItemCache,
    gridItemSource: GridItemSource?,
    textColor: TextColor,
    drag: Drag,
    dragIntOffset: IntOffset,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    folderDataById: FolderDataById?,
    homeSettings: HomeSettings,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    moveGridItemResult: MoveGridItemResult?,
    folderGridHorizontalPagerState: PagerState,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    statusBarNotifications: Map<String, Int>,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onMoveOutsideFolder: (GridItemSource) -> Unit,
    onResetOverlay: () -> Unit,
) {
    requireNotNull(gridItemSource)

    val context = LocalContext.current

    val density = LocalDensity.current

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val horizontalPagerPaddingDp = 50.dp

    val gridPaddingDp = 8.dp

    val gridPadding = with(density) {
        (horizontalPagerPaddingDp + gridPaddingDp).roundToPx()
    }

    val pageIndicatorHeight = 20.dp

    val pageIndicatorHeightPx = with(density) {
        pageIndicatorHeight.roundToPx()
    }

    val lastMoveGridItemResult = remember(key1 = moveGridItemResult) {
        if (moveGridItemResult != null && moveGridItemResult.isSuccess) {
            moveGridItemResult
        } else if (gridItemSource.gridItem.startColumn > -1 && gridItemSource.gridItem.startRow > -1) {
            MoveGridItemResult(
                isSuccess = true,
                movingGridItem = gridItemSource.gridItem,
                conflictingGridItem = null,
            )
        } else {
            null
        }
    }

    LaunchedEffect(key1 = drag, key2 = dragIntOffset) {
        handleDragFolderGridItem(
            density = density,
            currentPage = folderGridHorizontalPagerState.currentPage,
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            screenHeight = screenHeight,
            gridPadding = gridPadding,
            screenWidth = screenWidth,
            pageIndicatorHeight = pageIndicatorHeightPx,
            columns = homeSettings.folderColumns,
            rows = homeSettings.folderRows,
            isScrollInProgress = folderGridHorizontalPagerState.isScrollInProgress,
            paddingValues = paddingValues,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onMoveOutsideFolder = onMoveOutsideFolder,
            onUpdatePageDirection = { newPageDirection ->
                pageDirection = newPageDirection
            },
        )
    }

    LaunchedEffect(key1 = pageDirection) {
        handlePageDirection(
            currentPage = folderGridHorizontalPagerState.currentPage,
            pageDirection = pageDirection,
            onAnimateScrollToPage = { page ->
                folderGridHorizontalPagerState.animateScrollToPage(page = page)

                pageDirection = null
            },
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End, Drag.Cancel -> {
                handleDropFolderGridItem(
                    moveGridItemResult = moveGridItemResult,
                    density = density,
                    dragIntOffset = dragIntOffset,
                    screenHeight = screenHeight,
                    gridPadding = gridPadding,
                    pageIndicatorHeight = pageIndicatorHeightPx,
                    paddingValues = paddingValues,
                    onDragEnd = onDragEnd,
                    onDragCancel = {
                        Toast.makeText(
                            context,
                            "Can't place grid item at this position",
                            Toast.LENGTH_LONG,
                        ).show()

                        onDragCancel()
                    },
                )

                onResetOverlay()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            ),
    ) {
        HorizontalPager(
            state = folderGridHorizontalPagerState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                top = horizontalPagerPaddingDp,
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr) + horizontalPagerPaddingDp,
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr) + horizontalPagerPaddingDp,
                bottom = horizontalPagerPaddingDp,
            ),
        ) { index ->
            GridLayout(
                modifier = modifier
                    .fillMaxSize()
                    .padding(gridPaddingDp)
                    .background(
                        color = getSystemTextColor(textColor = textColor).copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = getSystemTextColor(textColor = textColor),
                        shape = RoundedCornerShape(8.dp),
                    ),
                gridItems = gridItemCache.folderGridItemsCacheByPage[index],
                columns = homeSettings.folderColumns,
                rows = homeSettings.folderRows,
                { gridItem ->
                    val gridItemSettings = if (gridItem.override) {
                        gridItem.gridItemSettings
                    } else {
                        homeSettings.gridItemSettings
                    }.run {
                        copy(
                            iconSize = iconSize / 2,
                            textSize = textSize / 2,
                        )
                    }

                    val textColor = if (gridItem.override) {
                        getGridItemTextColor(
                            systemTextColor = textColor,
                            gridItemTextColor = gridItem.gridItemSettings.textColor,
                        )
                    } else {
                        getSystemTextColor(textColor = textColor)
                    }

                    GridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = gridItemSettings,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        isDragging = gridItem.id == gridItemSource.gridItem.id,
                        hasShortcutHostPermission = hasShortcutHostPermission,
                        statusBarNotifications = statusBarNotifications,
                    )
                },
            )
        }

        PageIndicator(
            modifier = Modifier
                .height(pageIndicatorHeight)
                .fillMaxWidth(),
            pageCount = folderGridHorizontalPagerState.pageCount,
            currentPage = folderGridHorizontalPagerState.currentPage,
            pageOffset = folderGridHorizontalPagerState.currentPageOffsetFraction,
            color = getSystemTextColor(textColor = textColor),
        )
    }

    AnimatedDropGridItem(
        targetPage = folderGridHorizontalPagerState.currentPage,
        gridPadding = gridPadding,
        screenWidth = screenWidth,
        screenHeight = screenHeight,
        pageIndicatorHeight = pageIndicatorHeightPx,
        paddingValues = paddingValues,
        columns = homeSettings.folderColumns,
        rows = homeSettings.folderRows,
        overlayIntOffset = overlayIntOffset,
        overlayIntSize = overlayIntSize,
        textColor = textColor,
        iconPackInfoPackageName = iconPackInfoPackageName,
        hasShortcutHostPermission = hasShortcutHostPermission,
        gridItemSettings = homeSettings.gridItemSettings,
        drag = drag,
        moveGridItemResult = lastMoveGridItemResult,
        folderDataById = folderDataById,
        statusBarNotifications = statusBarNotifications,
    )
}
