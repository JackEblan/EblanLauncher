package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    gridItemContent: @Composable (
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    gridItemContent(
                        gridItem,
                        gridItem.startColumn * cellWidth,
                        gridItem.startRow * cellHeight,
                        gridItem.columnSpan * cellWidth,
                        gridItem.rowSpan * cellHeight,
                    )
                }.forEach { measurable ->
                    measurable.measure(
                        Constraints(
                            maxWidth = gridItem.columnSpan * cellWidth,
                            maxHeight = gridItem.rowSpan * cellHeight,
                        ),
                    ).placeRelative(
                        x = gridItem.startColumn * cellWidth,
                        y = gridItem.startRow * cellHeight,
                    )
                }
            }
        }
    }
}