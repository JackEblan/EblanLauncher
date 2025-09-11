package com.eblan.launcher.feature.home.component.grid

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
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridLayout(
    modifier: Modifier = Modifier,
    gridItems: List<GridItem>?,
    rows: Int,
    columns: Int,
    content: @Composable BoxScope.(GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    val width by animateIntAsState(targetValue = gridItem.columnSpan * cellWidth)

                    val height by animateIntAsState(targetValue = gridItem.rowSpan * cellHeight)

                    val x by animateIntAsState(targetValue = gridItem.startColumn * cellWidth)

                    val y by animateIntAsState(targetValue = gridItem.startRow * cellHeight)

                    Box(
                        modifier = Modifier.gridItem(
                            width = width,
                            height = height,
                            x = x,
                            y = y
                        ), content = {
                            content(gridItem)
                        }
                    )
                }.forEach { measurable ->
                    val gridItemParentData = measurable.parentData as GridItemParentData

                    measurable.measure(
                        Constraints.fixed(
                            width = gridItemParentData.width,
                            height = gridItemParentData.height,
                        ),
                    ).placeRelative(
                        x = gridItemParentData.x,
                        y = gridItemParentData.y
                    )
                }
            }
        }
    }
}

private data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

private fun Modifier.gridItem(
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