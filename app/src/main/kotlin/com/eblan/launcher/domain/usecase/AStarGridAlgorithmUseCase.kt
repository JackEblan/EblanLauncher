package com.eblan.launcher.domain.usecase

import com.eblan.launcher.GridItem
import com.eblan.launcher.GridRepository
import com.eblan.launcher.UserDataRepository
import com.eblan.launcher.domain.grid.gridAlgorithmUsingAStar
import com.eblan.launcher.domain.grid.isGridItemWithinBounds
import kotlinx.coroutines.flow.first

class AStarGridAlgorithmUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(page: Int, gridItem: GridItem) {
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
            return
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

        gridRepository.updateGridItems(gridItems = aStarGridItems)
    }
}