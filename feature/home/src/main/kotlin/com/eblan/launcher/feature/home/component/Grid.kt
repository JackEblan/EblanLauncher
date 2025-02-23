package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density

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
                    x = gridItemParentData.x, y = gridItemParentData.y,
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