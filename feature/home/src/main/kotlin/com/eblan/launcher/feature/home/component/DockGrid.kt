package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem

@Composable
fun DockGrid(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    dockGridItems: List<GridItem>,
    dockGridItemContent: @Composable (
        GridItem,
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
            dockGridItems.forEach { dockGridItem ->
                subcompose(dockGridItem.id) {
                    AnimatedGridItemContainer(
                        rowSpan = dockGridItem.rowSpan,
                        columnSpan = dockGridItem.columnSpan,
                        startRow = dockGridItem.startRow,
                        startColumn = dockGridItem.startColumn,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = {
                            dockGridItemContent(
                                dockGridItem,
                                dockGridItem.startColumn * cellWidth,
                                dockGridItem.startRow * cellHeight,
                                dockGridItem.columnSpan * cellWidth,
                                dockGridItem.rowSpan * cellHeight,
                            )
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