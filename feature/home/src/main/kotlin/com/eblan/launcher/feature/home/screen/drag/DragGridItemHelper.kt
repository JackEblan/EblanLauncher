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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import kotlinx.coroutines.delay

internal suspend fun handlePageDirection(
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

internal suspend fun handleDragGridItem(
    density: Density,
    currentPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    screenWidth: Int,
    screenHeight: Int,
    pageIndicatorHeight: Int,
    dockHeight: Dp,
    rows: Int,
    columns: Int,
    dockRows: Int,
    dockColumns: Int,
    isScrollInProgress: Boolean,
    gridItemSource: GridItemSource,
    paddingValues: PaddingValues,
    lockMovement: Boolean,
    screen: Screen,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    if (drag == Drag.None ||
        drag == Drag.End ||
        drag == Drag.Cancel ||
        isScrollInProgress
    ) {
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

    val dockHeightPx = with(density) {
        dockHeight.roundToPx()
    }

    val edgeDistance = with(density) {
        15.dp.roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnLeftGrid = dragIntOffset.x - edgeDistance < 0

    val isOnRightGrid = dragIntOffset.x + edgeDistance > gridWidth

    val isOnDock = dragY > (gridHeight - dockHeightPx)

    if (isOnLeftGrid) {
        delay(500L)

        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid) {
        delay(500L)

        onUpdatePageDirection(PageDirection.Right)
    } else if (isOnDock) {
        delay(100L)

        val cellWidth = gridWidth / dockColumns

        val cellHeight = dockHeightPx / dockRows

        val dockY = dragY - (gridHeight - dockHeightPx)

        val moveGridItem = getMoveGridItem(
            targetPage = currentPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = dockColumns,
            rows = dockRows,
            gridWidth = gridWidth,
            gridHeight = dockHeightPx,
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
            onUpdateSharedElementKey(
                SharedElementKey(
                    id = moveGridItem.id,
                    screen = screen,
                ),
            )

            onMoveGridItem(
                moveGridItem,
                dragX,
                dockY,
                dockColumns,
                dockRows,
                gridWidth,
                dockHeightPx,
                lockMovement,
            )
        }
    } else {
        delay(100L)

        val gridHeightWithPadding = gridHeight - pageIndicatorHeight - dockHeightPx

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeightWithPadding / rows

        val moveGridItem = getMoveGridItem(
            targetPage = currentPage,
            gridItem = gridItem,
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            columns = columns,
            rows = rows,
            gridWidth = gridWidth,
            gridHeight = gridHeightWithPadding,
            gridX = dragX,
            gridY = dragY,
            associate = Associate.Grid,
            gridItemSource = gridItemSource,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = moveGridItem,
            columns = columns,
            rows = rows,
        )

        if (isGridItemSpanWithinBounds) {
            onUpdateSharedElementKey(
                SharedElementKey(
                    id = moveGridItem.id,
                    screen = screen,
                ),
            )

            onMoveGridItem(
                moveGridItem,
                dragX,
                dragY,
                columns,
                rows,
                gridWidth,
                gridHeightWithPadding,
                lockMovement,
            )
        }
    }
}

internal suspend fun handleConflictingGridItem(
    drag: Drag,
    moveGridItemResult: MoveGridItemResult?,
    onShowFolderWhenDragging: (String) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
) {
    delay(1500L)

    if (drag != Drag.Dragging) return

    if (moveGridItemResult == null) return

    val conflictingGridItem = moveGridItemResult.conflictingGridItem

    if (conflictingGridItem != null) {
        onUpdateSharedElementKey(
            SharedElementKey(
                id = moveGridItemResult.movingGridItem.id,
                screen = Screen.FolderDrag,
            ),
        )

        onShowFolderWhenDragging(conflictingGridItem.id)
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
): GridItem = when (gridItemSource) {
    is GridItemSource.Existing -> {
        gridItem.copy(
            page = targetPage,
            startColumn = gridX / cellWidth,
            startRow = gridY / cellHeight,
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
): GridItem = when (val data = gridItem.data) {
    is GridItemData.Widget -> {
        val (checkedColumnSpan, checkedRowSpan) = getWidgetGridItemSpan(
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            minWidth = data.minWidth,
            minHeight = data.minHeight,
            targetCellWidth = data.targetCellWidth,
            targetCellHeight = data.targetCellHeight,
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
            columnSpan = checkedColumnSpan.coerceIn(1, columns),
            rowSpan = checkedRowSpan.coerceIn(1, rows),
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
