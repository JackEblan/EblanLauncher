package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.geometry.calculateMenuCoordinates
import com.eblan.launcher.domain.geometry.calculateResizableBoundingBox
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    gridItems: List<GridItem>?,
    gridItem: GridItem?,
    content: @Composable (BoxScope.() -> Unit),
    resizableContent: @Composable (
        id: Int,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    menuContent: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    GridItemContainer(
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
                        content = content,
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

            subcompose("Resizable") {
                if (gridItem != null) {
                    val width = gridItem.columnSpan * cellWidth

                    val height = gridItem.rowSpan * cellHeight

                    val x = gridItem.startColumn * cellWidth

                    val y = gridItem.startRow * cellHeight

                    resizableContent(
                        gridItem.id,
                        width,
                        height,
                        x,
                        y,
                        constraints.maxWidth,
                        constraints.maxHeight,
                    )
                }
            }.forEach { resizableMeasurable ->
                val gridItemParentData = resizableMeasurable.parentData as GridItemParentData

                val resizableBoundingBox = calculateResizableBoundingBox(
                    coordinates = Coordinates(
                        x = gridItemParentData.x, y = gridItemParentData.y,
                    ),
                    boundingBox = BoundingBox(
                        width = gridItemParentData.width, height = gridItemParentData.height,
                    ),
                )

                resizableMeasurable.measure(
                    Constraints(
                        minWidth = resizableBoundingBox.width,
                        minHeight = resizableBoundingBox.height,
                    ),
                ).placeRelative(x = resizableBoundingBox.x, y = resizableBoundingBox.y)

                val menuPlaceables = subcompose("Menu") {
                    menuContent()
                }.map { menuMeasurable ->
                    menuMeasurable.measure(Constraints())
                }

                menuPlaceables.forEach { menuPlaceable ->
                    val menuCoordinates = calculateMenuCoordinates(
                        parentX = gridItemParentData.x,
                        parentY = gridItemParentData.y,
                        parentWidth = gridItemParentData.width,
                        parentHeight = gridItemParentData.height,
                        childWidth = menuPlaceable.width,
                        childHeight = menuPlaceable.height,
                        screenWidth = constraints.maxWidth,
                        screenHeight = constraints.maxHeight,
                        margin = 100,
                    )

                    menuPlaceable.placeRelative(x = menuCoordinates.x, y = menuCoordinates.y)
                }
            }
        }
    }
}

@Composable
private fun GridItemContainer(
    modifier: Modifier = Modifier,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    content: @Composable (BoxScope.() -> Unit),
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Box(
        modifier = modifier.gridItem(
            width = width, height = height, x = x, y = y,
        ),
        content = content,
    )
}

data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

fun Modifier.gridItem(
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): Modifier = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemParentData(
                width = width, height = height, x = x, y = y,
            )
        }
    },
)