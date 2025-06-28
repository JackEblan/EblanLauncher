package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem

@Composable
fun DragGridSubcomposeLayout(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    selectedGridItem: GridItem?,
    gridItems: List<GridItem>?,
    selectedGridItemContent: @Composable BoxScope.() -> Unit,
    gridItemContent: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    if (selectedGridItem?.id != gridItem.id) {
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
                    } else {
                        GridItemContainer(
                            rowSpan = gridItem.rowSpan,
                            columnSpan = gridItem.columnSpan,
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            content = selectedGridItemContent,
                        )
                    }
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