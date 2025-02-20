package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.areValidCells
import com.eblan.launcher.domain.grid.isGridItemWithinBounds
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.model.GridItem
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
    suspend operator fun invoke(gridItem: GridItem) {
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

            val updatedGridItems = gridItemsWithValidCells.toMutableList().apply {
                set(oldGridItemIndex, gridItem)
            }

            val resolvedConflictsGridItems = resolveConflicts(
                page = gridItem.page,
                gridItems = updatedGridItems,
                movingGridItem = gridItem,
                rows = userData.rows,
                columns = userData.columns,
            )

            if (resolvedConflictsGridItems != null) {
                gridRepository.updateGridItems(gridItems = resolvedConflictsGridItems)
            }
        }
    }
}