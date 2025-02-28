package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    content: @Composable (
        id: Int,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
    ) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        val placeablesByPage = gridItems[page]?.map { gridItem ->
            subcompose(gridItem.id) {
                GridItemLayout(
                    id = gridItem.id,
                    cellWidth = cellWidth,
                    cellHeight = cellHeight,
                    startRow = gridItem.startRow,
                    startColumn = gridItem.startColumn,
                    rowSpan = gridItem.rowSpan,
                    columnSpan = gridItem.columnSpan,
                    content = content,
                )
            }.map { measurable ->
                val gridItemParentData = measurable.parentData as GridItemParentData

                measurable.measure(
                    Constraints(
                        maxWidth = gridItemParentData.width,
                        maxHeight = gridItemParentData.height,
                    ),
                )
            }
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeablesByPage?.forEach { placeables ->
                placeables.forEach { placeable ->
                    val gridItemParentData = placeable.parentData as GridItemParentData

                    placeable.placeRelative(
                        x = gridItemParentData.x,
                        y = gridItemParentData.y,
                    )
                }
            }
        }
    }
}

@Composable
fun GridItemLayout(
    modifier: Modifier = Modifier,
    id: Int,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    content: @Composable (
        id: Int,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
    ) -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Box(
        modifier = modifier.gridItem(
            width = width, height = height, x = x, y = y,
        ),
        content = {
            content(id, width, height, x, y)
        },
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