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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.GridItemContent
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.pageindicator.PageIndicator
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.screen.drag.handlePageDirection
import com.eblan.launcher.feature.home.util.getSystemTextColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun FolderDragScreen(
    modifier: Modifier = Modifier,
    gridItemsCacheByPage: Map<Int, List<GridItem>>,
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
) {
    requireNotNull(gridItemSource)

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

    LaunchedEffect(key1 = drag, key2 = dragIntOffset) {
        handleFolderDragIntOffset(
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
                handleOnDragEnd(
                    density = density,
                    dragIntOffset = dragIntOffset,
                    screenHeight = screenHeight,
                    gridPadding = gridPadding,
                    pageIndicatorHeight = pageIndicatorHeightPx,
                    paddingValues = paddingValues,
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                )
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
                gridItems = gridItemsCacheByPage[index],
                columns = homeSettings.folderColumns,
                rows = homeSettings.folderRows,
                { gridItem ->
                    GridItemContent(
                        gridItem = gridItem,
                        textColor = textColor,
                        gridItemSettings = homeSettings.gridItemSettings.copy(
                            iconSize = homeSettings.gridItemSettings.iconSize / 2,
                            textSize = homeSettings.gridItemSettings.textSize / 2,
                        ),
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        isDragging = gridItem.id == gridItemSource.gridItem.id,
                        hasShortcutHostPermission = hasShortcutHostPermission,
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
        moveGridItemResult = moveGridItemResult,
        folderDataById = folderDataById,
    )
}

@Composable
private fun AnimatedDropGridItem(
    modifier: Modifier = Modifier,
    targetPage: Int,
    gridPadding: Int,
    screenWidth: Int,
    screenHeight: Int,
    pageIndicatorHeight: Int,
    paddingValues: PaddingValues,
    columns: Int,
    rows: Int,
    overlayIntOffset: IntOffset,
    overlayIntSize: IntSize,
    textColor: TextColor,
    iconPackInfoPackageName: String,
    hasShortcutHostPermission: Boolean,
    gridItemSettings: GridItemSettings,
    drag: Drag,
    moveGridItemResult: MoveGridItemResult?,
    folderDataById: FolderDataById?,
) {
    if (drag != Drag.End ||
        moveGridItemResult?.isSuccess != true ||
        moveGridItemResult.movingGridItem.page != targetPage ||
        folderDataById == null
    ) {
        return
    }

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

    val gridLeft = leftPadding + gridPadding

    val gridTop = topPadding + gridPadding

    val gridWidthWithPadding = gridWidth - (gridPadding * 2)

    val gridHeightWithPadding = gridHeight - pageIndicatorHeight - (gridPadding * 2)

    val cellWidth = gridWidthWithPadding / columns

    val cellHeight = gridHeightWithPadding / rows

    val targetX = (moveGridItemResult.movingGridItem.startColumn * cellWidth) + gridLeft

    val targetY = (moveGridItemResult.movingGridItem.startRow * cellHeight) + gridTop

    val targetWidth = moveGridItemResult.movingGridItem.columnSpan * cellWidth

    val targetHeight = moveGridItemResult.movingGridItem.rowSpan * cellHeight

    val animatedX = remember { Animatable(overlayIntOffset.x.toFloat()) }

    val animatedY = remember { Animatable(overlayIntOffset.y.toFloat()) }

    val animatedWidth =
        remember { Animatable(overlayIntSize.width.toFloat()) }

    val animatedHeight =
        remember { Animatable(overlayIntSize.height.toFloat()) }

    val animatedAlpha = remember { Animatable(1f) }

    val gridItemSettingsConverter = TwoWayConverter<GridItemSettings, AnimationVector2D>(
        convertToVector = { settings ->
            AnimationVector2D(
                settings.iconSize.toFloat(),
                settings.textSize.toFloat(),
            )
        },
        convertFromVector = { vector ->
            gridItemSettings.copy(
                iconSize = vector.v1.roundToInt(),
                textSize = vector.v2.roundToInt(),
            )
        },
    )

    val animatedGridItemSettings = remember {
        Animatable(gridItemSettings, gridItemSettingsConverter)
    }

    LaunchedEffect(key1 = moveGridItemResult.movingGridItem) {
        launch { animatedX.animateTo(targetX.toFloat()) }

        launch { animatedY.animateTo(targetY.toFloat()) }

        launch { animatedWidth.animateTo(targetWidth.toFloat()) }

        launch { animatedHeight.animateTo(targetHeight.toFloat()) }

        launch {
            animatedGridItemSettings.animateTo(
                gridItemSettings.copy(
                    iconSize = gridItemSettings.iconSize / 2,
                    textSize = gridItemSettings.textSize / 2,
                ),
            )
        }
    }

    GridItemContent(
        modifier = modifier
            .offset {
                IntOffset(
                    x = animatedX.value.roundToInt(),
                    y = animatedY.value.roundToInt(),
                )
            }
            .alpha(animatedAlpha.value)
            .size(
                with(density) {
                    DpSize(
                        width = animatedWidth.value.roundToInt().toDp(),
                        height = animatedHeight.value.roundToInt().toDp(),
                    )
                },
            ),
        gridItem = moveGridItemResult.movingGridItem,
        textColor = textColor,
        gridItemSettings = animatedGridItemSettings.value,
        iconPackInfoPackageName = iconPackInfoPackageName,
        isDragging = false,
        hasShortcutHostPermission = hasShortcutHostPermission,
    )
}
