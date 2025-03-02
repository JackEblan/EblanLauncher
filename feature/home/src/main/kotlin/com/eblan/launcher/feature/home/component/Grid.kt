package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import com.eblan.launcher.domain.geometry.calculateResizableBoundingBox
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.BoundingBox
import com.eblan.launcher.domain.model.Coordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.feature.home.EmptyGridItem
import kotlin.math.roundToInt

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    resizedGridItemOverlay: GridItemOverlay?,
    showResize: Boolean,
    gridItems: Map<Int, List<GridItem>>,
    onResizeGridItem: (
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) -> Unit,
    onResizeEnd: () -> Unit,
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

                subcompose("Resize") {
                    if (showResize && resizedGridItemOverlay != null) {
                        GridItemResize(
                            page = page,
                            id = resizedGridItemOverlay.gridItem.id,
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = resizedGridItemOverlay.gridItem.startRow,
                            startColumn = resizedGridItemOverlay.gridItem.startColumn,
                            rowSpan = resizedGridItemOverlay.gridItem.rowSpan,
                            columnSpan = resizedGridItemOverlay.gridItem.columnSpan,
                            onResizeGridItem = onResizeGridItem,
                            onResizeEnd = onResizeEnd,
                        )
                    }
                }.forEach { measurable ->
                    val gridItemParentData = measurable.parentData as GridItemParentData

                    val resizableBoundingBox = calculateResizableBoundingBox(
                        coordinates = Coordinates(
                            x = gridItemParentData.x, y = gridItemParentData.y,
                        ),
                        boundingBox = BoundingBox(
                            width = gridItemParentData.width, height = gridItemParentData.height,
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

@Composable
private fun GridItemContainer(
    modifier: Modifier = Modifier,
    cellWidth: Int,
    cellHeight: Int,
    startRow: Int,
    startColumn: Int,
    rowSpan: Int,
    columnSpan: Int,
) {
    val width by animateIntAsState(targetValue = columnSpan * cellWidth)

    val height by animateIntAsState(targetValue = rowSpan * cellHeight)

    val x by animateIntAsState(targetValue = startColumn * cellWidth)

    val y by animateIntAsState(targetValue = startRow * cellHeight)

    Box(
        modifier = modifier.gridItem(
            width = width, height = height, x = x, y = y,
        ),
    ) {
        EmptyGridItem()
    }
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

    ResizableOverlay(
        modifier = modifier.gridItem(
            width = width,
            height = height,
            x = x,
            y = y,
        ),
        onDragEnd = onResizeEnd,
        onTopStartDrag = { change, dragAmount ->
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
        onTopEndDrag = { change, dragAmount ->
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
        onBottomStartDrag = { change, dragAmount ->
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
        onBottomEndDrag = { change, dragAmount ->
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