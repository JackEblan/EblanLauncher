package com.eblan.launcher.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItemPixel

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = {
        content()
    }, modifier = modifier) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            val gridItemPlacement = measurable.parentData as GridItemPlacementParentData

            measurable.measure(
                Constraints(
                    maxWidth = gridItemPlacement.boundingBox.width,
                    maxHeight = gridItemPlacement.boundingBox.height
                )
            )
        }

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            placeables.forEach { placeable ->
                val gridItemPlacement = placeable.parentData as GridItemPlacementParentData

                placeable.placeRelative(
                    x = gridItemPlacement.coordinates.x, y = gridItemPlacement.coordinates.y
                )
            }
        }
    }
}

data class GridItemPlacementParentData(
    val boundingBox: BoundingBox, val coordinates: Coordinates
)

fun Modifier.gridItemPlacement(gridItemPixel: GridItemPixel): Modifier =
    then(object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemPlacementParentData(
                boundingBox = gridItemPixel.boundingBox, coordinates = gridItemPixel.coordinates
            )
        }
    })