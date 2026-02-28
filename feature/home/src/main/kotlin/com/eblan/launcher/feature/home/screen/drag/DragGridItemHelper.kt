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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
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

internal fun handleAnimateScrollToPage(
    density: Density,
    paddingValues: PaddingValues,
    screenWidth: Int,
    dragIntOffset: IntOffset,
    associate: Associate?,
    gridItemSource: GridItemSource,
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    columns: Int,
    onUpdateGridPageDirection: (PageDirection?) -> Unit,
    onUpdateDockPageDirection: (PageDirection?) -> Unit,
    onUpdateFolderPageDirection: (PageDirection?) -> Unit,
) {
    val leftPadding = with(density) {
        paddingValues.calculateStartPadding(LayoutDirection.Ltr).roundToPx()
    }

    val rightPadding = with(density) {
        paddingValues.calculateEndPadding(LayoutDirection.Ltr).roundToPx()
    }

    val horizontalPadding = leftPadding + rightPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val edgeDistance = with(density) {
        20.dp.roundToPx()
    }

    val dragX = dragIntOffset.x - leftPadding

    when (gridItemSource) {
        is GridItemSource.Existing, is GridItemSource.New, is GridItemSource.Pin -> {
            val isOnLeftGrid = dragX < edgeDistance

            val isOnRightGrid = dragX > safeDrawingWidth - edgeDistance

            fun animateScrollToPage(onUpdatePageDirection: (PageDirection?) -> Unit) {
                if (isOnLeftGrid) {
                    onUpdatePageDirection(PageDirection.Left)
                } else if (isOnRightGrid) {
                    onUpdatePageDirection(PageDirection.Right)
                } else {
                    onUpdatePageDirection(null)
                }
            }

            when (associate) {
                Associate.Grid -> {
                    animateScrollToPage(onUpdatePageDirection = onUpdateGridPageDirection)
                }

                Associate.Dock -> {
                    animateScrollToPage(onUpdatePageDirection = onUpdateDockPageDirection)
                }

                null -> Unit
            }
        }

        is GridItemSource.Folder -> {
            val data = folderGridItem?.data as? GridItemData.Folder
                ?: error("Expected GridItemData.Folder")

            val folderCellWidth = safeDrawingWidth / columns

            val folderGridPaddingDp = 10.dp

            val folderGridPaddingPx = with(density) {
                folderGridPaddingDp.roundToPx()
            }

            val folderGridWidthPx = folderCellWidth * data.columns

            val centeredX =
                folderPopupIntOffset.x + (folderPopupIntSize.width / 2) - (folderGridWidthPx / 2)

            val popupX = centeredX.coerceIn(0, safeDrawingWidth - folderGridWidthPx)

            val folderDragX = dragX - popupX - folderGridPaddingPx

            val isOnLeftGrid = folderDragX < edgeDistance

            val isOnRightGrid = folderDragX > folderGridWidthPx - folderGridPaddingPx - edgeDistance

            if (isOnLeftGrid) {
                onUpdateFolderPageDirection(PageDirection.Left)
            } else if (isOnRightGrid) {
                onUpdateFolderPageDirection(PageDirection.Right)
            } else {
                onUpdateFolderPageDirection(null)
            }
        }
    }
}

internal suspend fun handleDragGridItem(
    density: Density,
    currentPage: Int,
    drag: Drag,
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
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    folderCurrentPage: Int,
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
    onUpdateAssociate: (Associate) -> Unit,
    onMoveFolderGridItem: (
        folderGridItem: GridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onMoveFolderGridItemOutsideFolder: (
        folderGridItem: GridItem,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
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

    val horizontalPadding = leftPadding + rightPadding

    val verticalPadding = topPadding + bottomPadding

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val dragX = dragIntOffset.x - leftPadding

    val dragY = dragIntOffset.y - topPadding

    val isOnDock = dockHeightPx > 0 && dragY > safeDrawingHeight - dockHeightPx

    when (gridItemSource) {
        is GridItemSource.Existing, is GridItemSource.New, is GridItemSource.Pin -> {
            if (isOnDock) {
                handleDragDockGridItem(
                    safeDrawingWidth = safeDrawingWidth,
                    dockColumns = dockColumns,
                    dockHeightPx = dockHeightPx,
                    dockRows = dockRows,
                    dragY = dragY,
                    safeDrawingHeight = safeDrawingHeight,
                    currentPage = currentPage,
                    dragX = dragX,
                    gridItemSource = gridItemSource,
                    screen = screen,
                    lockMovement = lockMovement,
                    onUpdateAssociate = onUpdateAssociate,
                    onMoveGridItem = onMoveGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            } else {
                handleDragGridItem(
                    safeDrawingHeight = safeDrawingHeight,
                    dockHeightPx = dockHeightPx,
                    pageIndicatorHeight = pageIndicatorHeight,
                    safeDrawingWidth = safeDrawingWidth,
                    columns = columns,
                    rows = rows,
                    currentPage = currentPage,
                    dragX = dragX,
                    dragY = dragY,
                    gridItemSource = gridItemSource,
                    screen = screen,
                    lockMovement = lockMovement,
                    onUpdateAssociate = onUpdateAssociate,
                    onMoveGridItem = onMoveGridItem,
                    onUpdateSharedElementKey = onUpdateSharedElementKey,
                )
            }
        }

        is GridItemSource.Folder -> {
            handleDragFolderGridItem(
                safeDrawingWidth = safeDrawingWidth,
                columns = columns,
                safeDrawingHeight = safeDrawingHeight,
                rows = rows,
                density = density,
                folderGridItem = folderGridItem,
                folderPopupIntOffset = folderPopupIntOffset,
                folderPopupIntSize = folderPopupIntSize,
                dragX = dragX,
                dragY = dragY,
                lockMovement = lockMovement,
                gridItemSource = gridItemSource,
                folderCurrentPage = folderCurrentPage,
                screen = screen,
                onMoveFolderGridItem = onMoveFolderGridItem,
                onUpdateSharedElementKey = onUpdateSharedElementKey,
                onMoveFolderGridItemOutsideFolder = onMoveFolderGridItemOutsideFolder,
                onUpdateGridItemSource = onUpdateGridItemSource,
            )
        }
    }
}

private suspend fun handleDragFolderGridItem(
    safeDrawingWidth: Int,
    columns: Int,
    safeDrawingHeight: Int,
    rows: Int,
    density: Density,
    folderGridItem: GridItem?,
    folderPopupIntOffset: IntOffset,
    folderPopupIntSize: IntSize,
    dragX: Int,
    dragY: Int,
    lockMovement: Boolean,
    gridItemSource: GridItemSource?,
    folderCurrentPage: Int,
    screen: Screen,
    onMoveFolderGridItem: (
        folderGridItem: GridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        currentPage: Int,
    ) -> Unit,
    onUpdateSharedElementKey: (SharedElementKey?) -> Unit,
    onMoveFolderGridItemOutsideFolder: (
        folderGridItem: GridItem,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
    ) -> Unit,
    onUpdateGridItemSource: (GridItemSource) -> Unit,
) {
    val data = folderGridItem?.data as? GridItemData.Folder ?: error("Expected GridItemData.Folder")

    val gridItemSourceFolder =
        gridItemSource as? GridItemSource.Folder ?: error("Expected GridItemSource.Folder")

    if (lockMovement) return

    delay(100L)

    val folderCellWidth = safeDrawingWidth / columns

    val folderCellHeight = safeDrawingHeight / rows

    val folderGridPaddingDp = 10.dp

    val folderGridPaddingPx = with(density) {
        folderGridPaddingDp.roundToPx()
    }

    val folderGridWidthPx = folderCellWidth * data.columns
    val folderGridHeightPx = folderCellHeight * data.rows

    val centeredX =
        folderPopupIntOffset.x + (folderPopupIntSize.width / 2) - (folderGridWidthPx / 2)
    val centeredY =
        folderPopupIntOffset.y + (folderPopupIntSize.height / 2) - (folderGridHeightPx / 2)

    val popupX = centeredX.coerceIn(0, safeDrawingWidth - folderGridWidthPx)
    val popupY = centeredY.coerceIn(0, safeDrawingHeight - folderGridHeightPx)

    val folderDragX = dragX - popupX - folderGridPaddingPx

    val folderDragY = dragY - popupY - folderGridPaddingPx

    val folderGridVisibleWidthPx = folderGridWidthPx - (folderGridPaddingPx * 2)
    val folderGridVisibleHeightPx = folderGridHeightPx - (folderGridPaddingPx * 2)

    val isInsideFolder = folderDragX in 0..folderGridVisibleWidthPx &&
            folderDragY in 0..folderGridVisibleHeightPx

    if (isInsideFolder) {
        onUpdateSharedElementKey(
            SharedElementKey(
                id = gridItemSourceFolder.applicationInfoGridItem.id,
                screen = screen,
            ),
        )

        onMoveFolderGridItem(
            gridItemSource.gridItem,
            data.gridItems,
            gridItemSourceFolder.applicationInfoGridItem,
            folderDragX,
            folderDragY,
            data.columns,
            data.rows,
            folderGridWidthPx,
            folderGridHeightPx,
            folderCurrentPage,
        )
    } else {
        onMoveFolderGridItemOutsideFolder(
            folderGridItem,
            gridItemSourceFolder.applicationInfoGridItem,
            data.gridItems,
        )

        onUpdateGridItemSource(
            GridItemSource.New(
                gridItem = GridItem(
                    id = gridItemSourceFolder.applicationInfoGridItem.id,
                    page = gridItemSourceFolder.applicationInfoGridItem.page,
                    startColumn = gridItemSourceFolder.applicationInfoGridItem.startColumn,
                    startRow = gridItemSourceFolder.applicationInfoGridItem.startRow,
                    columnSpan = gridItemSourceFolder.applicationInfoGridItem.columnSpan,
                    rowSpan = gridItemSourceFolder.applicationInfoGridItem.rowSpan,
                    data = GridItemData.ApplicationInfo(
                        serialNumber = gridItemSourceFolder.applicationInfoGridItem.serialNumber,
                        componentName = gridItemSourceFolder.applicationInfoGridItem.componentName,
                        packageName = gridItemSourceFolder.applicationInfoGridItem.packageName,
                        icon = gridItemSourceFolder.applicationInfoGridItem.icon,
                        label = gridItemSourceFolder.applicationInfoGridItem.label,
                        customIcon = gridItemSourceFolder.applicationInfoGridItem.customIcon,
                        customLabel = gridItemSourceFolder.applicationInfoGridItem.customLabel,
                        index = -1,
                        folderId = null,
                    ),
                    associate = gridItemSourceFolder.applicationInfoGridItem.associate,
                    override = gridItemSourceFolder.applicationInfoGridItem.override,
                    gridItemSettings = gridItemSourceFolder.applicationInfoGridItem.gridItemSettings,
                    doubleTap = gridItemSourceFolder.applicationInfoGridItem.doubleTap,
                    swipeUp = gridItemSourceFolder.applicationInfoGridItem.swipeUp,
                    swipeDown = gridItemSourceFolder.applicationInfoGridItem.swipeDown,
                ),
            ),
        )
    }
}

private suspend fun handleDragGridItem(
    safeDrawingHeight: Int,
    dockHeightPx: Int,
    pageIndicatorHeight: Int,
    safeDrawingWidth: Int,
    columns: Int,
    rows: Int,
    currentPage: Int,
    dragX: Int,
    dragY: Int,
    gridItemSource: GridItemSource,
    screen: Screen,
    lockMovement: Boolean,
    onUpdateAssociate: (Associate) -> Unit,
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
    val gridItem = requireNotNull(gridItemSource.gridItem)

    delay(100L)

    onUpdateAssociate(Associate.Grid)

    val gridHeightWithPadding = safeDrawingHeight - dockHeightPx - pageIndicatorHeight

    val cellWidth = safeDrawingWidth / columns

    val cellHeight = gridHeightWithPadding / rows

    val moveGridItem = getMoveGridItem(
        targetPage = currentPage,
        gridItem = gridItem,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        columns = columns,
        rows = rows,
        gridWidth = safeDrawingWidth,
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
            safeDrawingWidth,
            gridHeightWithPadding,
            lockMovement,
        )
    }
}

private suspend fun handleDragDockGridItem(
    safeDrawingWidth: Int,
    dockColumns: Int,
    dockHeightPx: Int,
    dockRows: Int,
    dragY: Int,
    safeDrawingHeight: Int,
    currentPage: Int,
    gridItemSource: GridItemSource,
    dragX: Int,
    screen: Screen,
    lockMovement: Boolean,
    onUpdateAssociate: (Associate) -> Unit,
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
    val gridItem = requireNotNull(gridItemSource.gridItem)

    delay(100L)

    onUpdateAssociate(Associate.Dock)

    val cellWidth = safeDrawingWidth / dockColumns

    val cellHeight = dockHeightPx / dockRows

    val dockY = dragY - (safeDrawingHeight - dockHeightPx)

    val moveGridItem = getMoveGridItem(
        targetPage = currentPage,
        gridItem = gridItem,
        cellWidth = cellWidth,
        cellHeight = cellHeight,
        columns = dockColumns,
        rows = dockRows,
        gridWidth = safeDrawingWidth,
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
            safeDrawingWidth,
            dockHeightPx,
            lockMovement,
        )
    }
}

internal suspend fun handleConflictingGridItem(
    gridItemSource: GridItemSource,
    drag: Drag,
    moveGridItemResult: MoveGridItemResult?,
    density: Density,
    screenWidth: Int,
    screenHeight: Int,
    paddingValues: PaddingValues,
    columns: Int,
    rows: Int,
    onShowFolderWhenDragging: (
        id: String,
        movingGridItem: GridItem,
        gridItemSource: GridItemSource,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    delay(1000L)

    if (drag != Drag.Dragging ||
        moveGridItemResult == null
    ) {
        return
    }

    if (gridItemSource.gridItem.data !is GridItemData.ApplicationInfo) {
        return
    }

    val conflictingGridItem = moveGridItemResult.conflictingGridItem ?: return

    val conflictingData = conflictingGridItem.data as? GridItemData.Folder ?: return

    val movingData =
        moveGridItemResult.movingGridItem.data as? GridItemData.ApplicationInfo ?: return

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

    val safeDrawingWidth = screenWidth - horizontalPadding

    val safeDrawingHeight = screenHeight - verticalPadding

    val cellWidth = safeDrawingWidth / columns

    val cellHeight = safeDrawingHeight / rows

    val x = conflictingGridItem.startColumn * cellWidth

    val y = conflictingGridItem.startRow * cellHeight

    val width = conflictingGridItem.columnSpan * cellWidth

    val height = conflictingGridItem.rowSpan * cellHeight

    onShowFolderWhenDragging(
        conflictingData.id,
        moveGridItemResult.movingGridItem,
        GridItemSource.Folder(
            gridItem = conflictingGridItem,
            applicationInfoGridItem = ApplicationInfoGridItem(
                id = moveGridItemResult.movingGridItem.id,
                page = moveGridItemResult.movingGridItem.page,
                startColumn = moveGridItemResult.movingGridItem.startColumn,
                startRow = moveGridItemResult.movingGridItem.startRow,
                columnSpan = moveGridItemResult.movingGridItem.columnSpan,
                rowSpan = moveGridItemResult.movingGridItem.rowSpan,
                associate = moveGridItemResult.movingGridItem.associate,
                componentName = movingData.componentName,
                packageName = movingData.packageName,
                icon = movingData.icon,
                label = movingData.label,
                override = moveGridItemResult.movingGridItem.override,
                serialNumber = movingData.serialNumber,
                customIcon = movingData.customIcon,
                customLabel = movingData.customLabel,
                gridItemSettings = moveGridItemResult.movingGridItem.gridItemSettings,
                doubleTap = moveGridItemResult.movingGridItem.doubleTap,
                swipeUp = moveGridItemResult.movingGridItem.swipeUp,
                swipeDown = moveGridItemResult.movingGridItem.swipeDown,
                index = conflictingData.gridItems.lastIndex + 1,
                folderId = conflictingData.id,
            ),
        ),
        IntOffset(
            x = x,
            y = y,
        ),
        IntSize(
            width = width,
            height = height,
        ),
    )
}

internal suspend fun handlePageDirection(
    pageDirection: PageDirection?,
    pagerState: PagerState,
) {
    if (pageDirection == null) return

    delay(500L)

    when (pageDirection) {
        PageDirection.Left -> {
            pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
        }

        PageDirection.Right -> {
            pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
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
): GridItem = when (gridItemSource) {
    is GridItemSource.Existing, is GridItemSource.Folder -> {
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
