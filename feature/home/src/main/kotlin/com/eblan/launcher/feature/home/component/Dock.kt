package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.DockItem

@Composable
fun Dock(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    dockItems: List<DockItem>,
    content: @Composable (
        dockItem: DockItem,
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
            dockItems.forEach { dockItem ->
                subcompose(dockItem.id) {
                    AnimatedGridItemContainer(
                        rowSpan = dockItem.rowSpan,
                        columnSpan = dockItem.columnSpan,
                        startRow = dockItem.startRow,
                        startColumn = dockItem.startColumn,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = {
                            content(
                                dockItem,
                                dockItem.startColumn * cellWidth,
                                dockItem.startRow * cellHeight,
                                dockItem.columnSpan * cellWidth,
                                dockItem.rowSpan * cellHeight,
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