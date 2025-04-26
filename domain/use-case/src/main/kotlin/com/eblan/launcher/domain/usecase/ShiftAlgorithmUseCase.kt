package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWithShift
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemShift
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShiftAlgorithmUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        rows: Int,
        columns: Int,
        movingGridItem: GridItem,
    ): List<GridItem>? {
        return withContext(Dispatchers.Default) {
            if (isGridItemSpanWithinBounds(
                    gridItem = movingGridItem,
                    rows = rows,
                    columns = columns,
                )
            ) {
                val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                    isGridItemSpanWithinBounds(
                        gridItem = gridItem,
                        rows = rows,
                        columns = columns,
                    ) && gridItem.page == movingGridItem.page
                }.toMutableList()

                var gridItemShift: GridItemShift? = null

                val index = gridItems.indexOfFirst { it.id == movingGridItem.id }

                if (index != -1) {
                    gridItemShift = getGridItemShift(
                        oldGridItem = gridItems[index],
                        newGridItem = movingGridItem,
                    )

                    gridItems[index] = movingGridItem
                } else {
                    gridItems.add(movingGridItem)
                }

                val resolvedConflictsGridItems = resolveConflictsWithShift(
                    gridItems = gridItems,
                    gridItemShift = gridItemShift,
                    movingGridItem = movingGridItem,
                    rows = rows,
                    columns = columns,
                )

                if (resolvedConflictsGridItems != null) {
                    gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                }

                resolvedConflictsGridItems
            } else {
                null
            }
        }
    }

    private fun getGridItemShift(
        oldGridItem: GridItem,
        newGridItem: GridItem,
    ): GridItemShift? {
        val oldCenterRow = oldGridItem.startRow + oldGridItem.rowSpan / 2.0
        val oldCenterColumn = oldGridItem.startColumn + oldGridItem.columnSpan / 2.0

        val newCenterRow = newGridItem.startRow + newGridItem.rowSpan / 2.0
        val newCenterColumn = newGridItem.startColumn + newGridItem.columnSpan / 2.0

        val rowDiff = newCenterRow - oldCenterRow
        val columnDiff = newCenterColumn - oldCenterColumn

        return when {
            rowDiff < 0 -> GridItemShift.Up
            rowDiff > 0 -> GridItemShift.Down
            columnDiff < 0 -> GridItemShift.Left
            columnDiff > 0 -> GridItemShift.Right
            else -> null
        }
    }
}