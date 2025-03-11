package com.eblan.launcher.feature.home.pager.component.grid

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.pager.component.menu.MenuPositionProvider
import kotlin.math.roundToInt

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    id: Int?,
    gridItems: Map<Int, List<GridItem>>,
    showMenu: Boolean,
    showResize: Boolean,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onDismissRequest: (() -> Unit)?,
    onResizeEnd: () -> Unit,
    gridItemContent: @Composable (gridItem: GridItem, width: Int, height: Int, x: Int, y: Int) -> Unit,
    menuContent: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    GridItemContainer(
                        gridItem = gridItem,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = gridItemContent,
                    )
                }.forEach { measurable ->
                    val gridItemParentData = measurable.parentData as GridItemParentData

                    measurable.measure(
                        Constraints(
                            maxWidth = gridItemParentData.width,
                            maxHeight = gridItemParentData.height,
                        ),
                    ).placeRelative(
                        x = gridItemParentData.x,
                        y = gridItemParentData.y,
                    )
                }

                val gridItemOverlay = gridItem.takeIf { it.id == id }

                if (showMenu && gridItemOverlay != null) {
                    subcompose("Menu") {
                        GridItemMenu(
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItemOverlay.startRow,
                            startColumn = gridItemOverlay.startColumn,
                            rowSpan = gridItemOverlay.rowSpan,
                            columnSpan = gridItemOverlay.columnSpan,
                            screenWidth = constraints.maxWidth,
                            screenHeight = constraints.maxHeight,
                            onDismissRequest = onDismissRequest,
                            content = menuContent,
                        )
                    }.forEach { measurable ->
                        measurable.measure(Constraints()).placeRelative(x = 0, y = 0)
                    }
                }

                if (showResize && gridItemOverlay != null) {
                    subcompose("Resize") {
                        GridItemResize(
                            page = page,
                            id = gridItemOverlay.id,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItemOverlay.startRow,
                            startColumn = gridItemOverlay.startColumn,
                            rowSpan = gridItemOverlay.rowSpan,
                            columnSpan = gridItemOverlay.columnSpan,
                            onResizeGridItem = onResizeGridItem,
                            onResizeEnd = onResizeEnd,
                        )
                    }.forEach { measurable ->
                        val gridItemParentData = measurable.parentData as GridItemParentData

                        val allowMeasurement =
                            gridItemParentData.width > cellWidth && gridItemParentData.height > cellHeight

                        val gridItemX = if (allowMeasurement) {
                            gridItemParentData.x
                        } else {
                            gridItemOverlay.startColumn * cellWidth
                        }

                        val gridItemY = if (allowMeasurement) {
                            gridItemParentData.y
                        } else {
                            gridItemOverlay.startRow * cellHeight
                        }

                        val gridItemWidth = if (allowMeasurement) {
                            gridItemParentData.width
                        } else {
                            gridItemOverlay.columnSpan * cellWidth
                        }

                        val gridItemHeight = if (allowMeasurement) {
                            gridItemParentData.height
                        } else {
                            gridItemOverlay.rowSpan * cellHeight
                        }

                        measurable.measure(
                            Constraints(
                                minWidth = gridItemWidth, minHeight = gridItemHeight
                            )
                        ).placeRelative(
                            x = gridItemX,
                            y = gridItemY,
                            zIndex = 1f,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridItemContainer(
    modifier: Modifier = Modifier,
    gridItem: GridItem,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    cellWidth: Int,
    cellHeight: Int,
    content: @Composable (gridItem: GridItem, width: Int, height: Int, x: Int, y: Int) -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Surface(
        modifier = modifier.gridItem(
            width = width, height = height, x = x, y = y,
        ),
        content = {
            content(gridItem, width, height, x, y)
        },
    )
}

@Composable
private fun GridItemMenu(
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    screenWidth: Int,
    screenHeight: Int,
    onDismissRequest: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    val width = columnSpan * cellWidth

    val height = rowSpan * cellHeight

    val x = startColumn * cellWidth

    val y = startRow * cellHeight

    Popup(
        popupPositionProvider = MenuPositionProvider(
            x = x,
            y = y,
            width = width,
            height = height,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        ),
        onDismissRequest = onDismissRequest,
        content = content,
    )
}

@Composable
private fun GridItemResize(
    modifier: Modifier = Modifier,
    page: Int,
    id: Int,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeGridItem: (
        page: Int,
        id: Int,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
) {
    val density = LocalDensity.current

    var width by remember { mutableIntStateOf(columnSpan * cellWidth) }

    var height by remember { mutableIntStateOf(rowSpan * cellHeight) }

    var x by remember { mutableIntStateOf(startColumn * cellWidth) }

    var y by remember { mutableIntStateOf(startRow * cellHeight) }

    val circleModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier
            .gridItem(
                width = width,
                height = height,
                x = x,
                y = y,
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
                                page,
                                id,
                                width,
                                height,
                                cellWidth,
                                cellHeight,
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
                                page,
                                id,
                                width,
                                height,
                                cellWidth,
                                cellHeight,
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
                                page,
                                id,
                                width,
                                height,
                                cellWidth,
                                cellHeight,
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
                                page,
                                id,
                                width,
                                height,
                                cellWidth,
                                cellHeight,
                                Anchor.TOP_START,
                            )
                        },
                    )
                },
        )
    }
}