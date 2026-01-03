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
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import kotlinx.coroutines.delay

internal suspend fun handleDragFolderGridItem(
    density: Density,
    currentPage: Int,
    drag: Drag,
    gridItem: GridItem,
    dragIntOffset: IntOffset,
    screenHeight: Int,
    screenWidth: Int,
    pageIndicatorHeight: Int,
    columns: Int,
    rows: Int,
    isScrollInProgress: Boolean,
    paddingValues: PaddingValues,
    titleHeight: Int,
    lockMovement: Boolean,
    folderId: String?,
    onUpdatePageDirection: (PageDirection) -> Unit,
    onMoveFolderGridItem: (
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) -> Unit,
    onMoveGridItemOutsideFolder: (
        gridItemSource: GridItemSource,
        folderId: String,
        movingGridItem: GridItem,
    ) -> Unit,
) {
    if (folderId == null ||
        drag != Drag.Dragging ||
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

    val edgeDistance = with(density) {
        30.dp.roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding - pageIndicatorHeight - titleHeight

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding - titleHeight

    val isOnLeftGrid = dragIntOffset.x - edgeDistance < 0

    val isOnRightGrid = dragIntOffset.x + edgeDistance > gridWidth

    val isOnTopGrid = dragIntOffset.y < topPadding + titleHeight

    if (isOnLeftGrid && !isOnTopGrid) {
        delay(1000L)

        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid && !isOnTopGrid) {
        delay(1000L)

        onUpdatePageDirection(PageDirection.Right)
    } else if (isOnTopGrid) {
        delay(100L)

        onMoveGridItemOutsideFolder(
            GridItemSource.Existing(
                gridItem = gridItem.copy(
                    startColumn = -1,
                    startRow = -1,
                    folderId = null,
                ),
            ),
            folderId,
            gridItem,
        )
    } else {
        delay(100L)

        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val newGridItem = gridItem.copy(
            folderId = folderId,
            page = currentPage,
            startColumn = dragX / cellWidth,
            startRow = dragY / cellHeight,
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
                dragX,
                dragY,
                columns,
                rows,
                gridWidth,
                gridHeight,
                lockMovement,
            )
        }
    }
}
