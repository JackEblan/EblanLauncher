package com.eblan.launcher.feature.home.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.GridItem

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    gridItemId: String?,
    gridItems: Map<Int, List<GridItem>>,
    showMenu: Boolean,
    onDismissRequest: () -> Unit,
    gridItemContent: @Composable (
        gridItem: GridItem,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) -> Unit,
    menuContent: @Composable (GridItem) -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        val cellWidth = constraints.maxWidth / columns

        val cellHeight = constraints.maxHeight / rows

        layout(width = constraints.maxWidth, height = constraints.maxHeight) {
            gridItems[page]?.forEach { gridItem ->
                subcompose(gridItem.id) {
                    gridItemContent(
                        gridItem,
                        gridItem.startColumn * cellWidth,
                        gridItem.startRow * cellHeight,
                        gridItem.columnSpan * cellWidth,
                        gridItem.rowSpan * cellHeight,
                    )
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

                if (showMenu && gridItemId == gridItem.id) {
                    subcompose("Menu") {
                        GridItemMenu(
                            cellWidth = cellWidth,
                            cellHeight = cellHeight,
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            rowSpan = gridItem.rowSpan,
                            columnSpan = gridItem.columnSpan,
                            onDismissRequest = onDismissRequest,
                            content = {
                                menuContent(gridItem)
                            },
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