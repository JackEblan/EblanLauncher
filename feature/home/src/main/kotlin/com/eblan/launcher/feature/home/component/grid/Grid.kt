package com.eblan.launcher.feature.home.component.grid

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.eblan.launcher.domain.geometry.calculateResizableBoundingBox
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.component.menu.MenuPositionProvider
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
    gridItemContent: @Composable () -> Unit,
    menuContent: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    GridItemContainer(
                        cellWidth = cellWidth,
                        cellHeight = cellHeight,
                        startRow = gridItem.startRow,
                        startColumn = gridItem.startColumn,
                        rowSpan = gridItem.rowSpan,
                        columnSpan = gridItem.columnSpan,
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
                    val width = gridItemOverlay.columnSpan * cellWidth

                    val height = gridItemOverlay.rowSpan * cellHeight

                    val x = gridItemOverlay.startColumn * cellWidth

                    val y = gridItemOverlay.startRow * cellHeight

                    subcompose("Menu") {
                        GridItemMenu(
                            popupPositionProvider = MenuPositionProvider(
                                x = x,
                                y = y,
                                width = width,
                                height = height,
                                screenWidth = constraints.maxWidth,
                                screenHeight = constraints.maxHeight,
                                margin = 0,
                            ),
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

                        val resizableBoundingBox = calculateResizableBoundingBox(
                            coordinates = Coordinates(
                                x = gridItemParentData.x, y = gridItemParentData.y,
                            ),
                            boundingBox = BoundingBox(
                                width = gridItemParentData.width,
                                height = gridItemParentData.height,
                            ),
                        )

                        measurable.measure(
                            Constraints(
                                minWidth = resizableBoundingBox.width,
                                minHeight = resizableBoundingBox.height,
                            ),
                        ).placeRelative(
                            x = resizableBoundingBox.x,
                            y = resizableBoundingBox.y,
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
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
    content: @Composable () -> Unit,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Surface(
        modifier = modifier.gridItem(
            width = width, height = height, x = x, y = y,
        ),
        content = content,
    )
}

@Composable
private fun GridItemMenu(
    popupPositionProvider: PopupPositionProvider,
    onDismissRequest: (() -> Unit)?,
    content: @Composable () -> Unit,
) {
    Popup(
        popupPositionProvider = popupPositionProvider,
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

    val commonModifier = Modifier
        .size(30.dp)
        .background(Color.White, shape = CircleShape)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        onResizeEnd()
                    },
                )
            }
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
                .then(commonModifier)
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
                .then(commonModifier)
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
                .then(commonModifier)
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
                .then(commonModifier)
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

data class GridItemParentData(
    val width: Int,
    val height: Int,
    val x: Int,
    val y: Int,
)

fun Modifier.gridItem(
    width: Int,
    height: Int,
    x: Int,
    y: Int,
): Modifier = then(
    object : ParentDataModifier {
        override fun Density.modifyParentData(parentData: Any?): Any {
            return GridItemParentData(
                width = width, height = height, x = x, y = y,
            )
        }
    },
)