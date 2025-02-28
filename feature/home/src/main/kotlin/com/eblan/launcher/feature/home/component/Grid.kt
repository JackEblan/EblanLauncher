package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import com.eblan.launcher.domain.model.GridItem

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = {
            content()
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            val gridItemParentData = measurable.parentData as GridItemParentData

            measurable.measure(
                Constraints(
                    maxWidth = gridItemParentData.width,
                    maxHeight = gridItemParentData.height,
                ),
            )
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
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

@Composable
fun GridSubcomposeLayout(
    page: Int,
    rows: Int,
    columns: Int,
    gridItems: Map<Int, List<GridItem>>,
    parentContent: @Composable () -> Unit,
    gridContent: @Composable (
        id: Int,
        width: Int,
        height: Int,
        x: Int,
        y: Int,
    ) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val parentPlaceables = subcompose(
            "Parent",
            parentContent,
        ).map { measurable -> measurable.measure(constraints) }

        val maxSize = parentPlaceables.fold(IntSize.Zero) { currentMax, placeable ->
            IntSize(
                width = maxOf(currentMax.width, placeable.width),
                height = maxOf(currentMax.height, placeable.height),
            )
        }

        val cellWidth = maxSize.width / columns

        val cellHeight = maxSize.height / rows

        val gridItemPlaceables = subcompose("GridItem") {
            gridItems[page]?.forEach { gridItem ->
                val width by animateIntAsState(targetValue = gridItem.columnSpan * cellWidth)

                val height by animateIntAsState(targetValue = gridItem.rowSpan * cellHeight)

                val x by animateIntAsState(targetValue = gridItem.startColumn * cellWidth)

                val y by animateIntAsState(targetValue = gridItem.startRow * cellHeight)

                gridContent(gridItem.id, width, height, x, y)
            }
        }.map { measurable ->
            val gridItemParentData = measurable.parentData as GridItemParentData

            measurable.measure(
                Constraints(
                    maxWidth = gridItemParentData.width,
                    maxHeight = gridItemParentData.height,
                ),
            )
        }

        layout(width = maxSize.width, height = maxSize.height) {
            parentPlaceables.forEach { placeable ->
                placeable.placeRelative(0, 0)
            }

            gridItemPlaceables.forEach { placeable ->
                val gridItemParentData = placeable.parentData as GridItemParentData

                placeable.placeRelative(
                    x = gridItemParentData.x,
                    y = gridItemParentData.y,
                )
            }
        }
    }
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