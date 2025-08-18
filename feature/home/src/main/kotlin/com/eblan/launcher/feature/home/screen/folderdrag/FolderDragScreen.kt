package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.NestedFolderGridItem
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.screen.drag.DragGridItem
import com.eblan.launcher.feature.home.screen.drag.handlePageDirection
import kotlin.math.roundToInt

@Composable
fun FolderDragScreen(
    modifier: Modifier = Modifier,
    startCurrentPage: Int,
    folderRows: Int,
    folderColumns: Int,
    gridItemsByPage: Map<Int, List<GridItem>>,
    gridItemSource: GridItemSource?,
    textColor: Long,
    drag: Drag,
    dragIntOffset: IntOffset,
    gridWidth: Int,
    gridHeight: Int,
    moveGridItemResult: MoveGridItemResult?,
    gridItemSettings: GridItemSettings,
    folderDataById: FolderDataById,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onDragEnd: (Int) -> Unit,
    onMoveOutsideFolder: (GridItemSource) -> Unit,
) {
    requireNotNull(gridItemSource)

    val density = LocalDensity.current

    var pageDirection by remember { mutableStateOf<PageDirection?>(null) }

    val horizontalPagerPaddingDp = 50.dp

    val gridPaddingDp = 8.dp

    val gridPaddingPx = with(density) {
        (horizontalPagerPaddingDp + gridPaddingDp).roundToPx()
    }

    val horizontalPagerState = rememberPagerState(
        initialPage = startCurrentPage,
        pageCount = {
            folderDataById.pageCount
        },
    )

    LaunchedEffect(key1 = dragIntOffset) {
        handleFolderDragIntOffset(
            targetPage = horizontalPagerState.currentPage,
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            rootHeight = gridHeight,
            gridPadding = gridPaddingPx,
            rootWidth = gridWidth,
            columns = folderColumns,
            rows = folderRows,
            isScrollInProgress = horizontalPagerState.isScrollInProgress,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onMoveOutsideFolder = onMoveOutsideFolder,
            onUpdatePageDirection = { newPageDirection ->
                pageDirection = newPageDirection
            },
        )
    }

    LaunchedEffect(key1 = pageDirection) {
        handlePageDirection(
            currentPage = horizontalPagerState.currentPage,
            pageDirection = pageDirection,
            onAnimateScrollToPage = { page ->
                horizontalPagerState.animateScrollToPage(page = page)

                pageDirection = null
            },
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End, Drag.Cancel -> {
                onDragEnd(horizontalPagerState.currentPage)
            }

            else -> Unit
        }
    }
    HorizontalPager(
        state = horizontalPagerState,
        contentPadding = PaddingValues(all = horizontalPagerPaddingDp),
    ) { index ->
        GridLayout(
            modifier = modifier
                .fillMaxSize()
                .padding(gridPaddingDp)
                .background(
                    color = Color(textColor).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                )
                .border(
                    width = 2.dp,
                    color = Color(textColor),
                    shape = RoundedCornerShape(8.dp),
                ),
            rows = folderRows,
            columns = folderColumns,
        ) {
            gridItemsByPage[index]?.forEach { gridItem ->
                FolderDragGridItemContent(
                    gridItem = gridItem,
                    textColor = textColor,
                    gridItemSource = gridItemSource,
                    gridItemSettings = gridItemSettings,
                )
            }
        }
    }

    if (drag == Drag.End &&
        moveGridItemResult != null &&
        moveGridItemResult.isSuccess
    ) {
        AnimatedDropGridItem(
            gridItem = moveGridItemResult.movingGridItem,
            gridPadding = gridPaddingPx,
            rootWidth = gridWidth,
            rootHeight = gridHeight,
            folderRows = folderRows,
            folderColumns = folderColumns,
            dragIntOffset = dragIntOffset,
            density = density,
            textColor = textColor,
            gridItemSettings = gridItemSettings,
        )
    }
}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun FolderDragGridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: Long,
    gridItemSource: GridItemSource,
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
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(currentTextColor),
                    ) {
                        ApplicationInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            textColor = currentTextColor,
                            gridItemSettings = currentGridItemSettings,
                        )
                    }
                }

                is GridItemData.Widget -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(currentTextColor),
                    ) {
                        WidgetGridItem(modifier = gridItemModifier, data = data)
                    }
                }

                is GridItemData.ShortcutInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(currentTextColor),
                    ) {
                        ShortcutInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            textColor = currentTextColor,
                            gridItemSettings = currentGridItemSettings,
                        )
                    }
                }

                is GridItemData.Folder -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(currentTextColor),
                    ) {
                        NestedFolderGridItem(
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
}

@Composable
private fun AnimatedDropGridItem(
    gridItem: GridItem,
    gridPadding: Int,
    rootWidth: Int,
    rootHeight: Int,
    folderRows: Int,
    folderColumns: Int,
    dragIntOffset: IntOffset,
    density: Density,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
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

    when (gridItem.associate) {
        Associate.Grid -> {
            val gridWidth = rootWidth - (gridPadding * 2)

            val gridHeight = rootHeight - (gridPadding * 2)

            val cellWidth = gridWidth / folderColumns

            val cellHeight = gridHeight / folderRows

            val x = gridItem.startColumn * cellWidth

            val y = gridItem.startRow * cellHeight

            val width = gridItem.columnSpan * cellWidth

            val height = gridItem.rowSpan * cellHeight

            val shadowX = dragIntOffset.x - (width / 2)

            val shadowY = dragIntOffset.y - (height / 2)

            val animatedX = remember {
                Animatable(shadowX.toFloat())
            }

            val animatedY = remember {
                Animatable(shadowY.toFloat())
            }

            LaunchedEffect(key1 = animatedX) {
                animatedX.animateTo(x.toFloat() + gridPadding)
            }

            LaunchedEffect(key1 = animatedY) {
                animatedY.animateTo(y.toFloat() + gridPadding)
            }

            val size = with(density) {
                DpSize(width = width.toDp(), height = height.toDp())
            }

            val gridItemModifier = Modifier
                .offset {
                    IntOffset(
                        x = animatedX.value.roundToInt(),
                        y = animatedY.value.roundToInt(),
                    )
                }
                .size(size)

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
                    NestedFolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        textColor = currentTextColor,
                        gridItemSettings = currentGridItemSettings,
                    )
                }
            }
        }

        Associate.Dock -> Unit
    }
}