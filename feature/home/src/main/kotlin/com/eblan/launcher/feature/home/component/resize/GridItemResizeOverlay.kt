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
import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun GridItemResizeOverlay(
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
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeGridItem: (
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
            if (dragHandle == Alignment.TopStart) {
                if (width >= dragHandleSizePx) {
                    x
                } else {
                    (startColumn * cellWidth) + (columnSpan * cellWidth) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.TopEnd) {
                if (width >= dragHandleSizePx) {
                    x
                } else {
                    startColumn * cellWidth
                }
            } else if (dragHandle == Alignment.BottomStart) {
                if (width >= dragHandleSizePx) {
                    x
                } else {
                    (startColumn * cellWidth) + (columnSpan * cellWidth) - dragHandleSizePx
                }
            } else {
                if (width >= dragHandleSizePx) {
                    x
                } else {
                    startColumn * cellWidth
                }
            }
        }
    }

    val borderY by remember {
        derivedStateOf {
            if (dragHandle == Alignment.TopStart) {
                if (height >= dragHandleSizePx) {
                    y
                } else {
                    (startRow * cellHeight) + (rowSpan * cellHeight) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.TopEnd) {
                if (height >= dragHandleSizePx) {
                    y
                } else {
                    (startRow * cellHeight) + (rowSpan * cellHeight) - dragHandleSizePx
                }
            } else if (dragHandle == Alignment.BottomStart) {
                if (height >= dragHandleSizePx) {
                    y
                } else {
                    startRow * cellHeight
                }
            } else {
                if (height >= dragHandleSizePx) {
                    y
                } else {
                    startRow * cellHeight
                }
            }
        }
    }

    LaunchedEffect(key1 = width, key2 = height) {
        delay(100)

        val allowedWidth = width.coerceAtLeast(cellWidth)

        val allowedHeight = height.coerceAtLeast(cellHeight)

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

        if (resizingGridItem != null) {
            onResizeGridItem(gridItems.orEmpty(), resizingGridItem, rows, columns)
        }
    }

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
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .size(dragHandleSize)
                .background(Color.White, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopStart
                        },
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            width += -dragAmountX
                            height += -dragAmountY

                            x += dragAmount.x.roundToInt()
                            y += dragAmount.y.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(15.dp, (-15).dp)
                .size(dragHandleSize)
                .background(Color.White, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.TopEnd
                        },
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            width += dragAmountX
                            height += -dragAmountY

                            y += dragAmount.y.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset((-15).dp, 15.dp)
                .size(dragHandleSize)
                .background(Color.White, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomStart
                        },
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            width += -dragAmountX
                            height += dragAmountY

                            x += dragAmount.x.roundToInt()
                        },
                    )
                },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(15.dp, 15.dp)
                .size(dragHandleSize)
                .background(Color.White, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            dragHandle = Alignment.BottomEnd
                        },
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().roundToPx()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().roundToPx()
                            }

                            width += dragAmountX
                            height += dragAmountY
                        },
                    )
                },
        )
    }
}