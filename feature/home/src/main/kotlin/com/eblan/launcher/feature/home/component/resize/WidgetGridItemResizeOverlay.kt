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
import com.eblan.launcher.domain.grid.resizeWidgetGridItemWithPixels
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun WidgetGridItemResizeOverlay(
    modifier: Modifier = Modifier,
    gridPadding: Int,
    gridItems: List<GridItem>?,
    gridItem: GridItem,
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    rows: Int,
    columns: Int,
    data: GridItemData.Widget,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeWidgetGridItem: (
        gridItems: List<GridItem>,
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val density = LocalDensity.current

    var width by remember { mutableIntStateOf(columnSpan * cellWidth) }

    var height by remember { mutableIntStateOf(rowSpan * cellHeight) }

    var x by remember { mutableIntStateOf(startColumn * cellWidth) }

    var y by remember { mutableIntStateOf(startRow * cellHeight) }

    var dragHandle by remember { mutableStateOf(Alignment.Center) }

    val dragHandleSize = 30.dp

    val dragHandleSizePx = with(density) {
        dragHandleSize.roundToPx()
    }

    val borderWidth by remember {
        derivedStateOf {
            with(density) {
                width.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderHeight by remember {
        derivedStateOf {
            with(density) {
                height.coerceAtLeast(dragHandleSizePx).toDp()
            }
        }
    }

    val borderX by remember {
        derivedStateOf {
            if (dragHandle == Alignment.CenterStart) {
                if (width >= dragHandleSizePx) {
                    x
                } else {
                    (startColumn * cellWidth) + (columnSpan * cellWidth) - dragHandleSizePx
                }
            } else {
                startColumn * cellWidth
            }
        }
    }

    val borderY by remember {
        derivedStateOf {
            if (dragHandle == Alignment.TopCenter) {
                if (height >= dragHandleSizePx) {
                    y
                } else {
                    (startRow * cellHeight) + (rowSpan * cellHeight) - dragHandleSizePx
                }
            } else {
                startRow * cellHeight
            }
        }
    }

    LaunchedEffect(key1 = width, key2 = height) {
        delay(100L)

        val allowedWidth = if (data.minResizeWidth > 0 && width <= data.minResizeWidth) {
            data.minResizeWidth
        } else if (data.maxResizeWidth in 1..<width) {
            data.maxResizeWidth
        } else {
            width
        }

        val allowedHeight = if (data.minResizeHeight > 0 && height <= data.minResizeHeight) {
            data.minResizeHeight
        } else if (data.maxResizeHeight in 1..<height) {
            data.maxResizeHeight
        } else {
            height
        }

        val resizingGridItem = when (dragHandle) {
            Alignment.TopCenter -> {
                resizeWidgetGridItemWithPixels(
                    gridItem = gridItem,
                    width = columnSpan * cellWidth,
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
                    height = rowSpan * cellHeight,
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
                    width = columnSpan * cellWidth,
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
                    height = rowSpan * cellHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = SideAnchor.Right,
                )
            }

            else -> null
        }

        if (resizingGridItem != null) {
            onResizeWidgetGridItem(
                gridItems.orEmpty(),
                resizingGridItem,
                rows,
                columns,
            )
        }
    }

    val circleModifier = Modifier
        .size(dragHandleSize)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = borderX + gridPadding,
                    y = borderY + gridPadding,
                )
            }
            .size(width = borderWidth, height = borderHeight)
            .border(width = 2.dp, color = Color.White),
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().roundToPx()
                                    }

                                    height += -dragAmountY

                                    y += dragAmount.y.roundToInt()
                                },
                            )
                        }
                } else this
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().roundToPx()
                                    }

                                    width += dragAmountX
                                },
                            )
                        }
                } else this
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().roundToPx()
                                    }

                                    height += dragAmountY
                                },
                            )
                        }
                } else this
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().roundToPx()
                                    }

                                    width += -dragAmountX

                                    x += dragAmount.x.roundToInt()
                                },
                            )
                        }
                } else this
            },
        )
    }
}