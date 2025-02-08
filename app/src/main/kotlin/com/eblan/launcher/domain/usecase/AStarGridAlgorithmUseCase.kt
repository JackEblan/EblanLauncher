package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.grid.isGridItemWithinBounds
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AStarGridAlgorithmUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(page: Int, gridItem: GridItem) {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val isGridItemWithinBounds = isGridItemWithinBounds(
                gridItem = gridItem,
                rows = userData.rows,
                columns = userData.columns,
            )

            if (isGridItemWithinBounds.not()) {
                return@withContext
            }

            val gridItemsWithValidCells = gridRepository.gridItems.first().filter { gridItem ->
                areValidCells(
                    gridCells = gridItem.cells, rows = userData.rows, columns = userData.columns
                )
            }

            val oldGridItem = gridItemsWithValidCells.find { it.id == gridItem.id }

            val oldGridItemIndex = gridItemsWithValidCells.indexOf(oldGridItem)

            val movingGridItem = gridItem.copy(page = page)

            val updatedGridItems = gridItemsWithValidCells.toMutableList().apply {
                set(oldGridItemIndex, movingGridItem)
            }

            val resolvedConflictsGridItems = resolveConflicts(
                page = page,
                gridItems = updatedGridItems,
                movingGridItem = movingGridItem,
                rows = userData.rows,
                columns = userData.columns,
            )

            if (resolvedConflictsGridItems != null) {
                gridRepository.updateGridItems(gridItems = resolvedConflictsGridItems)
            }
        }
    }
}