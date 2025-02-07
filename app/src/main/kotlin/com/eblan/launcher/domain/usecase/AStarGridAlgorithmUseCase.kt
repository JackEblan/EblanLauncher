package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.gridAlgorithmUsingAStar
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
            val gridItems = gridRepository.gridItems.first()

            val oldGridItem = gridItems.find { it.id == gridItem.id }

            val oldGridItemIndex = gridItems.indexOf(oldGridItem)

            val movingGridItem = gridItem.copy(page = page)

            val userData = userDataRepository.userData.first()

            val isGridItemWithinBounds = isGridItemWithinBounds(
                gridItem = gridItem,
                gridRows = userData.rows,
                gridCols = userData.columns,
            )

            if (isGridItemWithinBounds.not()) {
                return@withContext
            }

            val updatedGridItems = gridItems.toMutableList().apply {
                set(oldGridItemIndex, movingGridItem)
            }

            val aStarGridItems = gridAlgorithmUsingAStar(
                page = page,
                gridItems = updatedGridItems,
                movingGridItem = movingGridItem,
                gridRows = userData.rows,
                gridCols = userData.columns,
            )

            if (aStarGridItems != null) {
                gridRepository.updateGridItems(gridItems = aStarGridItems)
            }
        }
    }
}