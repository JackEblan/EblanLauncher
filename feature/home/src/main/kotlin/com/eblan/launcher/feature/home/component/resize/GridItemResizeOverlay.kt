package com.eblan.launcher.feature.home.component.resize

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
import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import kotlin.math.roundToInt

@Composable
fun GridItemResizeOverlay(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    gridWidth: Int,
    gridHeight: Int,
    cellWidth: Int,
    cellHeight: Int,
    rows: Int,
    columns: Int,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    color: Color,
    onResizeGridItem: (
        gridItem: GridItem,
        rows: Int,
        columns: Int,
    ) -> Unit,
    onResizeEnd: () -> Unit,
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
            if (dragHandle == Alignment.TopStart) {
                if (currentWidth >= dragHandleSizePx) {
                    currentX
                } else {
                    (x + width) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.TopEnd) {
                if (currentWidth >= dragHandleSizePx) {
                    currentX
                } else {
                    x
                }
            } else if (dragHandle == Alignment.BottomStart) {
                if (currentWidth >= dragHandleSizePx) {
                    currentX
                } else {
                    (x + width) - dragHandleSizePx
                }
            } else {
                if (currentWidth >= dragHandleSizePx) {
                    currentX
                } else {
                    x
                }
            }
        }
    }

    val borderY by remember {
        derivedStateOf {
            if (dragHandle == Alignment.TopStart) {
                if (currentHeight >= dragHandleSizePx) {
                    currentY
                } else {
                    (y + height) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.TopEnd) {
                if (currentHeight >= dragHandleSizePx) {
                    currentY
                } else {
                    (y + height) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.BottomStart) {
                if (currentHeight >= dragHandleSizePx) {
                    currentY
                } else {
                    y
                }
            } else {
                if (currentHeight >= dragHandleSizePx) {
                    currentY
                } else {
                    y
                }
            }
        }
    }

    var drag by remember { mutableStateOf(Drag.None) }

    val circleModifier = Modifier
        .size(dragHandleSize)
        .background(color = color, shape = CircleShape)

    LaunchedEffect(key1 = currentWidth, key2 = currentHeight) {
        val allowedWidth = currentWidth.coerceAtLeast(cellWidth)

        val allowedHeight = currentHeight.coerceAtLeast(cellHeight)

        val resizingGridItem = when (dragHandle) {
            Alignment.TopStart -> {
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = Anchor.BottomEnd,
                )
            }

            Alignment.TopEnd -> {
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = Anchor.BottomStart,
                )
            }

            Alignment.BottomStart -> {
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = Anchor.TopEnd,
                )
            }

            Alignment.BottomEnd -> {
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = allowedWidth,
                    height = allowedHeight,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                    anchor = Anchor.TopStart,
                )
            }

            else -> null
        }

        if (resizingGridItem != null && isGridItemSpanWithinBounds(
                gridItem = resizingGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            onResizeGridItem(resizingGridItem, rows, columns)
        }
    }

    LaunchedEffect(key1 = drag) {
        if (drag == Drag.End) {
            onResizeEnd()
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
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .then(circleModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopStart
                        },
                        onDragEnd = {
                            drag = Drag.End
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            currentWidth += -dragAmountX
                            currentHeight += -dragAmountY

                            currentX += dragAmount.x.roundToInt()
                            currentY += dragAmount.y.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(15.dp, (-15).dp)
                .then(circleModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopEnd
                        },
                        onDragEnd = {
                            drag = Drag.End
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            currentWidth += dragAmountX
                            currentHeight += -dragAmountY

                            currentY += dragAmount.y.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-15).dp, 15.dp)
                .then(circleModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomStart
                        },
                        onDragEnd = {
                            drag = Drag.End
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            currentWidth += -dragAmountX
                            currentHeight += dragAmountY

                            currentX += dragAmount.x.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(15.dp, 15.dp)
                .then(circleModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomEnd
                        },
                        onDragEnd = {
                            drag = Drag.End
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            currentWidth += dragAmountX
                            currentHeight += dragAmountY
                        },
                    )
                },
        )
    }
}