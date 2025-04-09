package com.eblan.launcher.feature.home.screen.grid.component

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.window.Popup
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.model.SideAnchor
import com.eblan.launcher.feature.home.screen.pager.component.GridItemResizeOverlay
import com.eblan.launcher.feature.home.screen.pager.component.MenuPositionProvider
import com.eblan.launcher.feature.home.screen.pager.component.WidgetGridItemResizeOverlay

@Composable
fun GridSubcomposeLayout(
    modifier: Modifier = Modifier,
    page: Int,
    rows: Int,
    columns: Int,
    lastGridItemDimensions: GridItemDimensions?,
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
    onResizeWidgetGridItem: (
        page: Int,
        gridItem: GridItem,
        widthPixel: Int,
        heightPixel: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: SideAnchor,
    ) -> Unit,
    onDismissRequest: () -> Unit,
    onResizeEnd: () -> Unit,
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
                val width = gridItem.columnSpan * cellWidth

                val height = gridItem.rowSpan * cellHeight

                val x = gridItem.startColumn * cellWidth

                val y = gridItem.startRow * cellHeight

                subcompose(gridItem.id) {
                    AnimatedGridItemContainer(
                        content = {
                            gridItemContent(
                                gridItem,
                                x,
                                y,
                                width,
                                height,
                                constraints.maxWidth,
                                constraints.maxHeight,
                            )
                        },
                    )
                }.forEach { measurable ->
                    measurable.measure(
                        Constraints(
                            maxWidth = width,
                            maxHeight = height,
                        ),
                    ).placeRelative(
                        x = x,
                        y = y,
                    )
                }

                val gridItemOverlay = lastGridItemDimensions?.gridItem?.id == gridItem.id

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

                if (showResize && gridItemOverlay) {
                    subcompose("Resize") {
                        when (val data = gridItem.data) {
                            is GridItemData.ApplicationInfo -> {
                                GridItemResizeOverlay(
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
                            }

                            is GridItemData.Widget -> {
                                WidgetGridItemResizeOverlay(
                                    page = page,
                                    gridItem = gridItem,
                                    cellWidth = cellWidth,
                                    cellHeight = cellHeight,
                                    data = data,
                                    startRow = gridItem.startRow,
                                    startColumn = gridItem.startColumn,
                                    rowSpan = gridItem.rowSpan,
                                    columnSpan = gridItem.columnSpan,
                                    onResizeWidgetGridItem = onResizeWidgetGridItem,
                                    onResizeEnd = onResizeEnd,
                                )
                            }
                        }
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
private fun AnimatedGridItemContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
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
