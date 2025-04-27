package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor

@Composable
fun ResizeGridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    gridItemId: String?,
    gridItems: Map<Int, List<GridItem>>,
    onResizeGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        gridWidth: Int,
        gridHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
    gridItemContent: @Composable BoxScope.(gridItem: GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    AnimatedGridItemContainer(
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = {
                            gridItemContent(
                                gridItem,
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

                if (gridItemId == gridItem.id) {
                    subcompose("Resize") {
                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                GridItemResizeOverlay(
                                    gridItem = gridItem,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    startRow = gridItem.startRow,
                                    startColumn = gridItem.startColumn,
                                    rowSpan = gridItem.rowSpan,
                                    columnSpan = gridItem.columnSpan,
                                    onResizeGridItem = { gridItem, width, height, anchor ->
                                        onResizeGridItem(
                                            gridItem,
                                            width,
                                            height,
                                            constraints.maxWidth,
                                            constraints.maxHeight,
                                            anchor,
                                        )
                                    },
                                    onResizeEnd = onResizeEnd,
                                )
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItemResizeOverlay(
                                    gridItem = gridItem,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    data = data,
                                    startRow = gridItem.startRow,
                                    startColumn = gridItem.startColumn,
                                    rowSpan = gridItem.rowSpan,
                                    columnSpan = gridItem.columnSpan,
                                    onResizeWidgetGridItem = { gridItem, width, height, anchor ->
                                        onResizeWidgetGridItem(
                                            gridItem,
                                            width,
                                            height,
                                            constraints.maxWidth,
                                            constraints.maxHeight,
                                            anchor,
                                        )
                                    },
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