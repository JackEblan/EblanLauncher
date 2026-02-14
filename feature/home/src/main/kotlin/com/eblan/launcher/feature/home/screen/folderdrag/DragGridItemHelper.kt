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
import com.eblan.launcher.feature.home.model.Screen
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.EDGE_DISTANCE
import kotlinx.coroutines.delay

internal fun handleAnimateScrollToPage(
    density: Density,
    paddingValues: PaddingValues,
    screenWidth: Int,
    dragIntOffset: IntOffset,
    onUpdatePageDirection: (PageDirection?) -> Unit,
) {
    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val edgeDistance = with(density) {
        EDGE_DISTANCE.dp.roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val gridWidth = screenWidth - horizontalPadding

    val isOnLeftGrid = dragIntOffset.x - edgeDistance < 0

    val isOnRightGrid = dragIntOffset.x + edgeDistance > gridWidth

    if (isOnLeftGrid) {
        onUpdatePageDirection(PageDirection.Left)
    } else if (isOnRightGrid) {
        onUpdatePageDirection(PageDirection.Right)
    } else {
        onUpdatePageDirection(null)
    }
}

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
    screen: Screen,
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
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onUpdateAssociate: (Associate) -> Unit,
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

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val gridWidth = screenWidth - horizontalPadding

    val gridHeight = screenHeight - verticalPadding - pageIndicatorHeight - titleHeight

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding - titleHeight

    val isOnTopGrid = dragIntOffset.y < topPadding + titleHeight

    if (isOnTopGrid) {
        delay(100L)

        onUpdateSharedElementKey(
            SharedElementKey(
                id = gridItem.id,
                screen = Screen.Drag,
            ),
        )

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

        onUpdateAssociate(Associate.Grid)

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
            onUpdateSharedElementKey(
                SharedElementKey(
                    id = newGridItem.id,
                    screen = screen,
                ),
            )

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
