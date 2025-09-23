/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
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
    columns: Int,
    rows: Int,
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
                            y = y,
                        ),
                        content = {
                            content(gridItem)
                        },
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
                        y = gridItemParentData.y,
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
