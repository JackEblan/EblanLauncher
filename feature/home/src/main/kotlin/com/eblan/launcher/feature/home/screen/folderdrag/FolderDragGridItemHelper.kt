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
package com.eblan.launcher.feature.home.screen.folderdrag

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

suspend fun handleFolderDragIntOffset(
    density: Density,
    targetPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    screenHeight: Int,
    gridPadding: Int,
    screenWidth: Int,
    pageIndicatorHeight: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    paddingValues: PaddingValues,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) -> Unit,
    onMoveOutsideFolder: (GridItemSource) -> Unit,
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

    val isOnBottomGrid = dragY > gridHeight - pageIndicatorHeight - gridPadding

    val isVerticalBounds = !isOnTopGrid && !isOnBottomGrid

    if (isOnLeftGrid && isVerticalBounds) {
        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid && isVerticalBounds) {
        onUpdatePageDirection(PageDirection.Right)
    } else if (!isVerticalBounds) {
        onMoveOutsideFolder(
            GridItemSource.Existing(gridItem = gridItem.copy(folderId = null)),
        )
    } else {
        val gridWidthWithPadding = gridWidth - (gridPadding * 2)

        val gridHeightWithPadding = (gridHeight - pageIndicatorHeight) - (gridPadding * 2)

        val gridX = dragX - gridPadding

        val gridY = dragY - gridPadding

        val cellWidth = gridWidthWithPadding / columns

        val cellHeight = gridHeightWithPadding / rows

        val newGridItem = gridItem.copy(
            page = targetPage,
            startColumn = gridX / cellWidth,
            startRow = gridY / cellHeight,
            associate = Associate.Grid,
        )

        val isGridItemSpanWithinBounds = isGridItemSpanWithinBounds(
            gridItem = newGridItem,
            columns = columns,
            rows = rows,
        )

        if (isGridItemSpanWithinBounds) {
            onMoveFolderGridItem(
                newGridItem,
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
