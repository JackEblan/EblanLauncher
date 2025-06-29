package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData

@Composable
fun ResizeGridSubcomposeLayout(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    gridItemId: Int?,
    gridItems: List<GridItem>?,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: () -> Unit,
    gridItemContent: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
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

                if (gridItemId == gridItem.id) {
                    subcompose("Resize") {
                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                GridItemResizeOverlay(
                                    gridItem = gridItem,
                                    gridWidth = constraints.maxWidth,
                                    gridHeight = constraints.maxHeight,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    rows = rows,
                                    columns = columns,
                                    startRow = gridItem.startRow,
                                    startColumn = gridItem.startColumn,
                                    rowSpan = gridItem.rowSpan,
                                    columnSpan = gridItem.columnSpan,
                                    onResizeGridItem = onResizeGridItem,
                                    onResizeEnd = onResizeEnd,
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItemResizeOverlay(
                                    gridItem = gridItem,
                                    gridWidth = constraints.maxWidth,
                                    gridHeight = constraints.maxHeight,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    rows = rows,
                                    columns = columns,
                                    data = data,
                                    startRow = gridItem.startRow,
                                    startColumn = gridItem.startColumn,
                                    rowSpan = gridItem.rowSpan,
                                    columnSpan = gridItem.columnSpan,
                                    onResizeWidgetGridItem = onResizeGridItem,
                                    onResizeEnd = onResizeEnd,
                                )
                            }
                        }
                    }.forEach { measurable ->
                        val gridItemParentData = measurable.parentData as GridItemParentData

                        measurable.measure(
                            Constraints(
                                minWidth = gridItemParentData.width,
                                minHeight = gridItemParentData.height,
                            ),
                        ).placeRelative(
                            x = gridItemParentData.x,
                            y = gridItemParentData.y,
                            zIndex = 1f,
                        )
                    }
                }
            }
        }
    }
}