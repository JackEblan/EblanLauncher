package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.component.grid.ApplicationInfoGridItem
import com.eblan.launcher.feature.home.component.grid.GridLayout
import com.eblan.launcher.feature.home.component.grid.NestedFolderGridItem
import com.eblan.launcher.feature.home.component.grid.ShortcutInfoGridItem
import com.eblan.launcher.feature.home.component.grid.WidgetGridItem
import com.eblan.launcher.feature.home.component.grid.gridItem
import com.eblan.launcher.feature.home.model.Drag

@Composable
fun FolderDragScreen(
    modifier: Modifier = Modifier,
    folderRows: Int,
    folderColumns: Int,
    gridItems: List<GridItem>?,
    gridItem: GridItem?,
    textColor: Long,
    drag: Drag,
    dragIntOffset: IntOffset,
    rootWidth: Int,
    rootHeight: Int,
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
) {
    requireNotNull(gridItem)

    requireNotNull(gridItems)

    val density = LocalDensity.current

    val gridPaddingDp = 20.dp

    val gridPaddingPx = with(density) {
        gridPaddingDp.roundToPx()
    }

    LaunchedEffect(key1 = dragIntOffset) {
        handleFolderDragIntOffset(
            drag = drag,
            gridItem = gridItem,
            dragIntOffset = dragIntOffset,
            rootHeight = rootHeight,
            gridPadding = gridPaddingPx,
            rootWidth = rootWidth,
            columns = folderColumns,
            rows = folderRows,
            onMoveFolderGridItem = onMoveFolderGridItem,
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
            gridItems.forEach { gridItem ->
                GridItemContent(
                    gridItem = gridItem,
                    color = Color(textColor),
                )
            }
        }
    }

}

@Composable
@OptIn(ExperimentalSharedTransitionApi::class)
private fun GridItemContent(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    color: Color,
) {
    key(gridItem.id) {
        LookaheadScope {
            val gridItemModifier = modifier
                .animateBounds(this)
                .gridItem(gridItem)

            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    ApplicationInfoGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = color,
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
                        color = color,
                    )
                }

                is GridItemData.Folder -> {
                    NestedFolderGridItem(
                        modifier = gridItemModifier,
                        data = data,
                        color = color,
                    )
                }
            }
        }
    }
}