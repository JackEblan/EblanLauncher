/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.feature.home.screen.drag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

suspend fun handlePageDirection(
    currentPage: Int,
    pageDirection: PageDirection?,
    onAnimateScrollToPage: suspend (Int) -> Unit,
) {
    when (pageDirection) {
        PageDirection.Left -> {
            onAnimateScrollToPage(currentPage - 1)
        }

        PageDirection.Right -> {
            onAnimateScrollToPage(currentPage + 1)
        }

        null -> Unit
    }
}

suspend fun handleDragIntOffset(
    density: Density,
    targetPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    screenWidth: Int,
    screenHeight: Int,
    pageIndicatorHeight: Int,
    dockHeight: Int,
    gridPadding: Int,
    rows: Int,
    columns: Int,
    dockRows: Int,
    dockColumns: Int,
    isScrollInProgress: Boolean,
    gridItemSource: GridItemSource,
    paddingValues: PaddingValues,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
) {
    delay(1000L)

    if (drag != Drag.Dragging || isScrollInProgress) {
        return
    }

    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    val bottomPadding = with(density) {
        paddingValues.calculateBottomPadding().roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnLeftGrid = dragX < gridPadding

    val isOnRightGrid = dragX > gridWidth - gridPadding

    val isOnTopGrid = dragY < gridPadding

    val isOnBottomGrid = dragY > gridHeight - dockHeight - gridPadding

    val isHorizontalBounds = !isOnLeftGrid && !isOnRightGrid

    val isVerticalBounds = !isOnTopGrid && !isOnBottomGrid

    val isOnDock = dragY > (gridHeight - dockHeight)

    if (isOnLeftGrid && isVerticalBounds) {
        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid && isVerticalBounds) {
        onUpdatePageDirection(PageDirection.Right)
    } else if (isOnDock) {
        val cellWidth = gridWidth / dockColumns

        val cellHeight = dockHeight / dockRows

        val dockY = dragY - (gridHeight - dockHeight)

        val moveGridItem = getMoveGridItem(
            targetPage = targetPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = dockColumns,
            rows = dockRows,
            gridWidth = gridWidth,
            gridHeight = dockHeight,
            gridX = dragX,
            gridY = dockY,
            associate = Associate.Dock,
            gridItemSource = gridItemSource,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = moveGridItem,
            columns = dockColumns,
            rows = dockRows,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveGridItem(
                moveGridItem,
                dragX,
                dockY,
                dockColumns,
                dockRows,
                gridWidth,
                dockHeight,
            )
        }
    } else if (isHorizontalBounds && isVerticalBounds) {
        val gridWidthWithPadding = gridWidth - (gridPadding * 2)

        val gridHeightWithPadding = (gridHeight - pageIndicatorHeight - dockHeight) - (gridPadding * 2)

        val gridX = dragX - gridPadding

        val gridY = dragY - gridPadding

        val cellWidth = gridWidthWithPadding / columns

        val cellHeight = gridHeightWithPadding / rows

        val moveGridItem = getMoveGridItem(
            targetPage = targetPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = columns,
            rows = rows,
            gridWidth = gridWidthWithPadding,
            gridHeight = gridHeightWithPadding,
            gridX = gridX,
            gridY = gridY,
            associate = Associate.Grid,
            gridItemSource = gridItemSource,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = moveGridItem,
            columns = columns,
            rows = rows,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveGridItem(
                moveGridItem,
                gridX,
                gridY,
                columns,
                rows,
                gridWidthWithPadding,
                gridHeightWithPadding,
            )
        }
    }
}

private fun getMoveGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
    gridItemSource: GridItemSource,
): GridItem {
    return when (gridItemSource) {
        is GridItemSource.Existing -> {
            getMoveExistingGridItem(
                targetPage = targetPage,
                gridItem = gridItem,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                gridX = gridX,
                gridY = gridY,
                associate = associate,
            )
        }

        is GridItemSource.New, is GridItemSource.Pin -> {
            getMoveNewGridItem(
                targetPage = targetPage,
                gridItem = gridItem,
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                columns = columns,
                rows = rows,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                gridX = gridX,
                gridY = gridY,
                associate = associate,
            )
        }
    }
}

private fun getMoveNewGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellHeight: Int,
    cellWidth: Int,
    columns: Int,
    rows: Int,
    gridWidth: Int,
    gridHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
): GridItem {
    return when (val data = gridItem.data) {
        is GridItemData.Widget -> {
            val (checkedColumnSpan, checkedRowSpan) = getWidgetGridItemSpan(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                minHeight = data.minHeight,
                minWidth = data.minWidth,
                targetCellHeight = data.targetCellHeight,
                targetCellWidth = data.targetCellWidth,
            )

            val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
                columns = columns,
                rows = rows,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
                minWidth = data.minWidth,
                minHeight = data.minHeight,
                targetCellWidth = data.targetCellWidth,
                targetCellHeight = data.targetCellHeight,
            )

            val newData = data.copy(
                minWidth = checkedMinWidth,
                minHeight = checkedMinHeight,
            )

            gridItem.copy(
                page = targetPage,
                startColumn = gridX / cellWidth,
                startRow = gridY / cellHeight,
                columnSpan = checkedColumnSpan,
                rowSpan = checkedRowSpan,
                data = newData,
                associate = associate,
            )
        }

        else -> {
            gridItem.copy(
                page = targetPage,
                startColumn = gridX / cellWidth,
                startRow = gridY / cellHeight,
                associate = associate,
            )
        }
    }
}

private fun getMoveExistingGridItem(
    targetPage: Int,
    gridItem: GridItem,
    cellWidth: Int,
    cellHeight: Int,
    gridX: Int,
    gridY: Int,
    associate: Associate,
) = when (gridItem.data) {
    is GridItemData.Widget -> {
        gridItem.copy(
            page = targetPage,
            startColumn = gridX / cellWidth,
            startRow = gridY / cellHeight,
            associate = associate,
        )
    }

    else -> {
        gridItem.copy(
            page = targetPage,
            startColumn = gridX / cellWidth,
            startRow = gridY / cellHeight,
            associate = associate,
        )
    }
}
