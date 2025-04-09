package com.eblan.launcher.feature.home.screen.grid.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem

@Composable
fun SimpleGridSubcomposeLayout(
    modifier: Modifier = Modifier,
    index: Int,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemContent: @Composable (gridItem: GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[index]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    AnimatedGridItemContainer(
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = {
                            gridItemContent(gridItem)
                        },
                    )
                }.forEach { measurable ->
                    val gridItemParentData = measurable.parentData as GridItemParentData

                    measurable.measure(
                        Constraints(
                            maxWidth = gridItemParentData.width,
                            maxHeight = gridItemParentData.height,
                        ),
                    ).placeRelative(
                        x = gridItemParentData.x,
                        y = gridItemParentData.y,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedGridItemContainer(
    modifier: Modifier = Modifier,
    rowSpan: Int,
    columnSpan: Int,
    startRow: Int,
    startColumn: Int,
    cellWidth: Int,
    cellHeight: Int,
    content: @Composable () -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Surface(
        modifier = modifier.animateGridItemPlacement(
            width = width,
            height = height,
            x = x,
            y = y,
        ),
        content = content,
    )
}