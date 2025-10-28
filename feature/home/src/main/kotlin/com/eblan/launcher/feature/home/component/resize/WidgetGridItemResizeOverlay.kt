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
package com.eblan.launcher.feature.home.component.resize

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resizeWidgetGridItemWithPixels
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.model.Drag
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WidgetGridItemResizeOverlay(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridWidth: Int,
    gridHeight: Int,
    rows: Int,
    columns: Int,
    data: GridItemData.Widget,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Color,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        columns: Int,
        rows: Int,
    ) -> Unit,
    onResizeEnd: (GridItem) -> Unit,
) {
    val density = LocalDensity.current

    var currentX by remember { mutableIntStateOf(x) }

    var currentY by remember { mutableIntStateOf(y) }

    var currentWidth by remember { mutableIntStateOf(width) }

    var currentHeight by remember { mutableIntStateOf(height) }

    var dragHandle by remember { mutableStateOf(Alignment.Center) }

    val dragHandleSize = 30.dp

    val dragHandleSizePx = with(density) {
        dragHandleSize.roundToPx()
    }

    val borderWidth by remember {
        derivedStateOf {
            with(density) {
                currentWidth.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderHeight by remember {
        derivedStateOf {
            with(density) {
                currentHeight.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderX by remember {
        derivedStateOf {
            if (dragHandle == Alignment.CenterStart) {
                if (currentWidth >= dragHandleSizePx) {
                    currentX
                } else {
                    (x + width) - dragHandleSizePx
                }
            } else {
                x
            }
        }
    }

    val borderY by remember {
        derivedStateOf {
            if (dragHandle == Alignment.TopCenter) {
                if (currentHeight >= dragHandleSizePx) {
                    currentY
                } else {
                    (y + height) - dragHandleSizePx
                }
            } else {
                y
            }
        }
    }

    var drag by remember { mutableStateOf(Drag.None) }

    val circleModifier = Modifier
        .size(dragHandleSize)
        .background(color = color, shape = CircleShape)

    LaunchedEffect(key1 = currentWidth, key2 = currentHeight) {
        delay(100L)

        val allowedWidth = if (data.minResizeWidth > 0 && currentWidth <= data.minResizeWidth) {
            data.minResizeWidth
        } else if (data.maxResizeWidth in 1..<currentWidth) {
            data.maxResizeWidth
        } else {
            currentWidth
        }

        val allowedHeight = if (data.minResizeHeight > 0 && currentHeight <= data.minResizeHeight) {
            data.minResizeHeight
        } else if (data.maxResizeHeight in 1..<currentHeight) {
            data.maxResizeHeight
        } else {
            currentHeight
        }

        val resizingGridItem = when (dragHandle) {
            Alignment.TopCenter -> {
                resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = SideAnchor.Bottom,
                )
            }

            Alignment.CenterEnd -> {
                resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = height,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = SideAnchor.Left,
                )
            }

            Alignment.BottomCenter -> {
                resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = SideAnchor.Top,
                )
            }

            Alignment.CenterStart -> {
                resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = height,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = SideAnchor.Right,
                )
            }

            else -> null
        }

        if (resizingGridItem != null && isGridItemSpanWithinBounds(
                gridItem = resizingGridItem,
                columns = columns,
                rows = rows,
            )
        ) {
            onResizeWidgetGridItem(
                resizingGridItem,
                columns,
                rows,
            )
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.End) {
            onResizeEnd(gridItem)
        }
    }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = borderX,
                    y = borderY,
                )
            }
            .size(width = borderWidth, height = borderHeight)
            .border(width = 2.dp, color = color),
    ) {
        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.TopCenter)
                        .offset(y = (-15).dp)
                        .then(circleModifier)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.TopCenter
                                },
                                onDragEnd = {
                                    drag = Drag.End
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().roundToPx()
                                    }

                                    currentHeight += -dragAmountY

                                    currentY += dragAmount.y.roundToInt()
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.CenterEnd)
                        .offset(15.dp)
                        .then(circleModifier)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.CenterEnd
                                },
                                onDragEnd = {
                                    drag = Drag.End
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().roundToPx()
                                    }

                                    currentWidth += dragAmountX
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_VERTICAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.BottomCenter)
                        .offset(y = 15.dp)
                        .then(circleModifier)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.BottomCenter
                                },
                                onDragEnd = {
                                    drag = Drag.End
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().roundToPx()
                                    }

                                    currentHeight += dragAmountY
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )

        Box(
            modifier = Modifier.run {
                if (data.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL || data.resizeMode == AppWidgetProviderInfo.RESIZE_BOTH) {
                    align(Alignment.CenterStart)
                        .offset((-15).dp)
                        .then(circleModifier)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragHandle = Alignment.CenterStart
                                },
                                onDragEnd = {
                                    drag = Drag.End
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().roundToPx()
                                    }

                                    currentWidth += -dragAmountX

                                    currentX += dragAmount.x.roundToInt()
                                },
                            )
                        }
                } else {
                    this
                }
            },
        )
    }
}
