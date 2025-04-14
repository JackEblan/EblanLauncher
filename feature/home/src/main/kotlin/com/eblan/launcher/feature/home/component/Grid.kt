package com.eblan.launcher.feature.home.component

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemLayoutInfo

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    lastGridItemLayoutInfo: GridItemLayoutInfo?,
    gridItems: Map<Int, List<GridItem>>,
    showMenu: Boolean,
    onDismissRequest: () -> Unit,
    gridItemContent: @Composable (
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
    ) -> Unit,
    menuContent: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    Surface {
                        gridItemContent(
                            gridItem,
                            gridItem.startColumn * cellWidth,
                            gridItem.startRow * cellHeight,
                            gridItem.columnSpan * cellWidth,
                            gridItem.rowSpan * cellHeight,
                            constraints.maxWidth,
                            constraints.maxHeight,
                        )
                    }
                }.forEach { measurable ->
                    measurable.measure(
                        Constraints(
                            maxWidth = gridItem.columnSpan * cellWidth,
                            maxHeight = gridItem.rowSpan * cellHeight,
                        ),
                    ).placeRelative(
                        x = gridItem.startColumn * cellWidth,
                        y = gridItem.startRow * cellHeight,
                    )
                }

                val gridItemOverlay = lastGridItemLayoutInfo?.gridItem?.id == gridItem.id

                if (showMenu && gridItemOverlay) {
                    subcompose("Menu") {
                        GridItemMenu(
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            rowSpan = gridItem.rowSpan,
                            columnSpan = gridItem.columnSpan,
                            onDismissRequest = onDismissRequest,
                            content = menuContent,
                        )
                    }.forEach { measurable ->
                        measurable.measure(Constraints()).placeRelative(x = 0, y = 0)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedGridItemContainer(
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
    onDismissRequest: () -> Unit,
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
        ),
        onDismissRequest = onDismissRequest,
        content = content,
    )
}