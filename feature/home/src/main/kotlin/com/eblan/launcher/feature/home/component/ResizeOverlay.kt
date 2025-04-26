package com.eblan.launcher.feature.home.component

import android.appwidget.AppWidgetProviderInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.SideAnchor
import kotlin.math.roundToInt

@Composable
fun GridItemResizeOverlay(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val density = LocalDensity.current

    var width by remember { mutableIntStateOf(columnSpan * cellWidth) }

    var height by remember { mutableIntStateOf(rowSpan * cellHeight) }

    var x by remember { mutableIntStateOf(startColumn * cellWidth) }

    var y by remember { mutableIntStateOf(startRow * cellHeight) }

    val allowX by remember {
        derivedStateOf {
            width >= cellWidth / 2
        }
    }

    val allowY by remember {
        derivedStateOf {
            height >= cellHeight / 2
        }
    }

    val circleModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier
            .animatedGridItemPlacement(
                width = if (allowX) {
                    width
                } else {
                    columnSpan * cellWidth
                },
                height = if (allowY) {
                    height
                } else {
                    rowSpan * cellHeight
                },
                x = if (allowX) {
                    x
                } else {
                    startColumn * cellWidth
                },
                y = if (allowY) {
                    y
                } else {
                    startRow * cellHeight
                },
            )
            .border(width = 2.dp, color = Color.White),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset((-15).dp, (-15).dp)
                .then(circleModifier)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx().roundToInt()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx().roundToInt()
                            }

                            width += -dragAmountX
                            height += -dragAmountY

                            x += dragAmount.x.roundToInt()
                            y += dragAmount.y.roundToInt()

                            onResizeGridItem(
                                gridItem,
                                width,
                                height,
                                Anchor.BOTTOM_END,
                            )
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
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx().roundToInt()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx().roundToInt()
                            }

                            width += dragAmountX
                            height += -dragAmountY

                            y += dragAmount.y.roundToInt()

                            onResizeGridItem(
                                gridItem,
                                width,
                                height,
                                Anchor.BOTTOM_START,
                            )

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
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx().roundToInt()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx().roundToInt()
                            }

                            width += -dragAmountX
                            height += dragAmountY

                            x += dragAmount.x.roundToInt()

                            onResizeGridItem(
                                gridItem,
                                width,
                                height,
                                Anchor.TOP_END,
                            )
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
                        onDragEnd = onResizeEnd,
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val dragAmountX = with(density) {
                                dragAmount.x.toDp().toPx().roundToInt()
                            }

                            val dragAmountY = with(density) {
                                dragAmount.y.toDp().toPx().roundToInt()
                            }

                            width += dragAmountX
                            height += dragAmountY

                            onResizeGridItem(
                                gridItem,
                                width,
                                height,
                                Anchor.TOP_START,
                            )
                        },
                    )
                },
        )
    }
}

@Composable
fun WidgetGridItemResizeOverlay(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    data: GridItemData.Widget,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeWidgetGridItem: (
        gridItem: GridItem,
        width: Int,
        height: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val density = LocalDensity.current

    var width by remember { mutableIntStateOf(columnSpan * cellWidth) }

    var height by remember { mutableIntStateOf(rowSpan * cellHeight) }

    var x by remember { mutableIntStateOf(startColumn * cellWidth) }

    var y by remember { mutableIntStateOf(startRow * cellHeight) }

    val allowX by remember {
        derivedStateOf {
            if (data.minResizeWidth > 0 && data.maxResizeWidth > 0) {
                width in data.minResizeWidth..data.maxResizeWidth
            } else {
                width >= cellWidth / 2
            }
        }
    }

    val allowY by remember {
        derivedStateOf {
            if (data.minResizeHeight > 0 && data.maxResizeHeight > 0) {
                height in data.minResizeHeight..data.maxResizeHeight
            } else {
                height >= cellHeight / 2
            }
        }
    }

    val circleModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier
            .animatedGridItemPlacement(
                width = if (allowX) {
                    width
                } else {
                    columnSpan * cellWidth
                },
                height = if (allowY) {
                    height
                } else {
                    rowSpan * cellHeight
                },
                x = if (allowX) {
                    x
                } else {
                    startColumn * cellWidth
                },
                y = if (allowY) {
                    y
                } else {
                    startRow * cellHeight
                },
            )
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().toPx().roundToInt()
                                    }

                                    height += -dragAmountY

                                    y += dragAmount.y.roundToInt()

                                    onResizeWidgetGridItem(
                                        gridItem,
                                        width,
                                        height,
                                        SideAnchor.BOTTOM,
                                    )
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().toPx().roundToInt()
                                    }

                                    width += dragAmountX

                                    onResizeWidgetGridItem(
                                        gridItem,
                                        width,
                                        height,
                                        SideAnchor.LEFT,
                                    )
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountY = with(density) {
                                        dragAmount.y.toDp().toPx().roundToInt()
                                    }

                                    height += dragAmountY

                                    onResizeWidgetGridItem(
                                        gridItem,
                                        width,
                                        height,
                                        SideAnchor.TOP,
                                    )
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
                                onDragEnd = onResizeEnd,
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragAmountX = with(density) {
                                        dragAmount.x.toDp().toPx().roundToInt()
                                    }

                                    width += -dragAmountX

                                    x += dragAmount.x.roundToInt()

                                    onResizeWidgetGridItem(
                                        gridItem,
                                        width,
                                        height,
                                        SideAnchor.RIGHT,
                                    )
                                },
                            )
                        }
                } else this
            },
        )
    }
}