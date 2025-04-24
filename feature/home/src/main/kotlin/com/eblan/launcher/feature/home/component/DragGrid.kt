package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.framework.windowmanager.ScreenSize

@Composable
fun DragGridSubcomposeLayout(
    modifier: Modifier = Modifier,
    index: Int,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    screenSize: ScreenSize,
    gridItemContent: @Composable (gridItem: GridItem) -> Unit,
) {
    val cellWidth = screenSize.width / columns

    val cellHeight = screenSize.height / rows

    SubcomposeLayout(modifier = modifier) { constraints ->
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