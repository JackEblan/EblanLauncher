package com.eblan.launcher.domain.usecase

import com.eblan.launcher.GridItem
import com.eblan.launcher.GridRepository
import com.eblan.launcher.domain.grid.AStarGridAlgorithm
import kotlinx.coroutines.flow.first

class AStarGridAlgorithmUseCase(
    private val gridRepository: GridRepository,
    private val aStarGridAlgorithm: AStarGridAlgorithm
) {
    suspend operator fun invoke(page: Int, gridItem: GridItem) {
        val oldGridItem = gridRepository.gridItems.first().find { it.id == gridItem.id }

        val oldGridItemIndex = gridRepository.gridItems.first().indexOf(oldGridItem)

        val movingGridItem = gridItem.copy(page = page)

        val updatedGridItems = gridRepository.gridItems.first().toMutableList().apply {
            set(oldGridItemIndex, movingGridItem)
        }

        val aStarGridItems = aStarGridAlgorithm(
            page = page,
            gridItems = updatedGridItems,
            movingGridItem = movingGridItem,
        )

        gridRepository.updateGridItems(gridItems = aStarGridItems)
    }
}