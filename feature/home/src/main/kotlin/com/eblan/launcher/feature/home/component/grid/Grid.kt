package com.eblan.launcher.feature.home.component.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            measurables.forEach { measurable ->
                val gridItemParentData = measurable.parentData as GridItemParentData

                measurable.measure(
                    Constraints(
                        maxWidth = gridItemParentData.width,
                        maxHeight = gridItemParentData.height,
                    ),
                ).placeRelative(
                    x = gridItemParentData.x, y = gridItemParentData.y,
                )
            }
        }
    }
}

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    gridItems: List<GridItem>,
    content: @Composable (GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems.forEach { gridItem ->
                subcompose(
                    slotId = gridItem.id,
                    content = {
                        content(gridItem)
                    },
                ).forEach { measurable ->
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
                width = width,
                height = height,
                x = x,
                y = y,
            )
        }
    },
)