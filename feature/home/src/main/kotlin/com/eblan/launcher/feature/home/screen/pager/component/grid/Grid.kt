package com.eblan.launcher.feature.home.screen.pager.component.grid

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
import androidx.compose.runtime.derivedStateOf
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
import com.eblan.launcher.feature.home.screen.pager.component.menu.MenuPositionProvider
import kotlin.math.roundToInt

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    currentGridItem: GridItem?,
    gridItems: Map<Int, List<GridItem>>,
    showMenu: Boolean,
    showResize: Boolean,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
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
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        content = {
                            gridItemContent(
                                gridItem, gridItem.columnSpan * cellWidth,
                                gridItem.rowSpan * cellHeight,
                                gridItem.startColumn * cellWidth,
                                gridItem.startRow * cellHeight,
                            )
                        },
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

                val gridItemOverlay = gridItem.id == currentGridItem?.id

                if (showMenu && gridItemOverlay) {
                    subcompose("Menu") {
                        GridItemMenu(
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            rowSpan = gridItem.rowSpan,
                            columnSpan = gridItem.columnSpan,
                            screenWidth = constraints.maxWidth,
                            screenHeight = constraints.maxHeight,
                            onDismissRequest = onDismissRequest,
                            content = menuContent,
                        )
                    }.forEach { measurable ->
                        measurable.measure(Constraints()).placeRelative(x = 0, y = 0)
                    }
                }

                if (showResize && gridItemOverlay) {
                    subcompose("Resize") {
                        GridItemResize(
                            page = page,
                            gridItem = gridItem,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            rowSpan = gridItem.rowSpan,
                            columnSpan = gridItem.columnSpan,
                            onResizeGridItem = onResizeGridItem,
                            onResizeEnd = onResizeEnd,
                        )
                    }.forEach { measurable ->
                        val gridItemParentData = measurable.parentData as GridItemParentData

                        measurable.measure(
                            Constraints(
                                minWidth = gridItemParentData.width,
                                minHeight = gridItemParentData.height,
                            ),
                        ).placeRelative(
                            x = gridItemParentData.x,
                            y = gridItemParentData.y,
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
    rowSpan: Int,
    columnSpan: Int,
    startRow: Int,
    startColumn: Int,
    cellWidth: Int,
    cellHeight: Int,
    content: @Composable () -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Surface(
        modifier = modifier.animateGridItemPlacement(
            width = width,
            height = height,
            x = x,
            y = y,
        ),
        content = content,
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
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    onResizeGridItem: (
        page: Int,
        gridItem: GridItem,
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
            .animateGridItemPlacement(
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
                                page,
                                gridItem,
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
                                gridItem,
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
                                gridItem,
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
                                gridItem,
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