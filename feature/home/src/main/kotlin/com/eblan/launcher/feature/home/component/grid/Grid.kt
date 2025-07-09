package com.eblan.launcher.feature.home.component.grid

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    rows: Int,
    columns: Int,
    content: @Composable () -> Unit,
) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            measurables.forEach { measurable ->
                val gridItem = (measurable.parentData as GridItemParentData).gridItem

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                measurable.measure(
                    Constraints(
                        maxWidth = width,
                        maxHeight = height,
                    ),
                ).placeRelative(x = x, y = y)
            }
        }
    }
}

private data class GridItemParentData(
    val gridItem: GridItem,
)

fun Modifier.gridItem(gridItem: GridItem): Modifier = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemParentData(gridItem = gridItem)
        }
    },
)