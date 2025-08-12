package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.FolderGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.NestedFolderGridItem
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.screen.drag.DragGridItem
import kotlin.math.roundToInt

@Composable
fun FolderDragScreen(
    modifier: Modifier = Modifier,
    folderRows: Int,
    folderColumns: Int,
    gridItems: List<GridItem>?,
    gridItemSource: GridItemSource?,
    textColor: Long,
    drag: Drag,
    dragIntOffset: IntOffset,
    rootWidth: Int,
    rootHeight: Int,
    moveGridItemResult: MoveGridItemResult?,
    gridItemSettings: GridItemSettings,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onDragEnd: () -> Unit,
    onMoveOutsideFolder: (GridItemSource) -> Unit,
) {
    requireNotNull(gridItemSource)

    requireNotNull(gridItems)

    val density = LocalDensity.current

    val verticalGridPaddingDp = 80.dp

    val horizontalGridPaddingDp = 5.dp

    val verticalGridPaddingPx = with(density) {
        verticalGridPaddingDp.roundToPx()
    }

    val horizontalGridPaddingPx = with(density) {
        horizontalGridPaddingDp.roundToPx()
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleFolderDragIntOffset(
            drag = drag,
            gridItem = gridItemSource.gridItem,
            dragIntOffset = dragIntOffset,
            rootHeight = rootHeight,
            verticalGridPadding = verticalGridPaddingPx,
            horizontalGridPadding = horizontalGridPaddingPx,
            rootWidth = rootWidth,
            columns = folderColumns,
            rows = folderRows,
            onMoveFolderGridItem = onMoveFolderGridItem,
            onMoveOutsideFolder = onMoveOutsideFolder,
        )
    }

    LaunchedEffect(key1 = drag) {
        when (drag) {
            Drag.End, Drag.Cancel -> {
                onDragEnd()
            }

            else -> Unit
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        GridLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    vertical = verticalGridPaddingDp,
                    horizontal = horizontalGridPaddingDp,
                )
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
            gridItems.forEach { gridItem ->
                GridItemContent(
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
            verticalGridPaddingPx = verticalGridPaddingPx,
            horizontalGridPaddingPx = horizontalGridPaddingPx,
            rootWidth = rootWidth,
            rootHeight = rootHeight,
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
private fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    textColor: Long,
    gridItemSource: GridItemSource,
    gridItemSettings: GridItemSettings,
) {
    key(gridItem.id) {
        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(textColor),
                    ) {
                        ApplicationInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            iconSize = gridItemSettings.iconSize,
                            textColor = textColor,
                            textSize = gridItemSettings.textSize,
                        )
                    }
                }

                is GridItemData.Widget -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(textColor),
                    ) {
                        WidgetGridItem(
                            modifier = gridItemModifier,
                            data = data,
                        )
                    }
                }

                is GridItemData.ShortcutInfo -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(textColor),
                    ) {
                        ShortcutInfoGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            color = Color(textColor),
                        )
                    }
                }

                is GridItemData.Folder -> {
                    DragGridItem(
                        modifier = gridItemModifier,
                        isDragging = gridItemSource.gridItem.id == gridItem.id,
                        color = Color(textColor),
                    ) {
                        NestedFolderGridItem(
                            modifier = gridItemModifier,
                            data = data,
                            color = Color(textColor),
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
    verticalGridPaddingPx: Int,
    horizontalGridPaddingPx: Int,
    rootWidth: Int,
    rootHeight: Int,
    folderRows: Int,
    folderColumns: Int,
    dragIntOffset: IntOffset,
    density: Density,
    textColor: Long,
    gridItemSettings: GridItemSettings,
) {
    when (gridItem.associate) {
        Associate.Grid -> {
            val gridWidth = rootWidth - (horizontalGridPaddingPx * 2)

            val gridHeight = rootHeight - (verticalGridPaddingPx * 2)

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
                animatedX.animateTo(x.toFloat() + horizontalGridPaddingPx)
            }

            LaunchedEffect(key1 = animatedY) {
                animatedY.animateTo(y.toFloat() + verticalGridPaddingPx)
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
                        iconSize = gridItemSettings.iconSize,
                        textColor = textColor,
                        textSize = gridItemSettings.textSize,
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
                        color = Color(textColor),
                    )
                }

                is GridItemData.Folder -> {
                    FolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = Color(textColor),
                    )
                }
            }
        }

        Associate.Dock -> Unit
    }
}