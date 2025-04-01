package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWithShift
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemShift
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AStarGridAlgorithmUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(gridItem: GridItem): List<GridItem>? {
        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            if (isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                )
            ) {
                val gridItems = gridRepository.gridItems.first().toMutableList()

                var gridItemShift: GridItemShift? = null

                val index = gridItems.indexOfFirst { it.id == gridItem.id }

                if (index != -1) {
                    gridItemShift = getGridItemShift(
                        oldGridItem = gridItems[index],
                        newGridItem = gridItem,
                    )

                    gridItems[index] = gridItem
                } else {
                    gridItems.add(gridItem)
                }

                val resolvedConflictsGridItems = resolveConflictsWithShift(
                    gridItems = gridItems,
                    gridItemShift = gridItemShift,
                    movingGridItem = gridItem,
                    rows = userData.rows,
                    columns = userData.columns,
                )

                if (resolvedConflictsGridItems != null) {
                    gridRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
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