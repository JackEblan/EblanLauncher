package com.eblan.launcher.domain.usecase

import com.eblan.launcher.GridItem
import com.eblan.launcher.GridRepository
import com.eblan.launcher.UserDataRepository
import com.eblan.launcher.domain.grid.canGridItemFitAnywhere
import com.eblan.launcher.domain.grid.gridAlgorithmUsingAStar
import com.eblan.launcher.domain.grid.isGridItemWithinBounds
import kotlinx.coroutines.flow.first

class AStarGridAlgorithmUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(page: Int, gridItem: GridItem) {
        val oldGridItem = gridRepository.gridItems.first().find { it.id == gridItem.id }

        val oldGridItemIndex = gridRepository.gridItems.first().indexOf(oldGridItem)

        val movingGridItem = gridItem.copy(page = page)

        val userData = userDataRepository.userData.first()

        val canGridItemFitAnywhere = canGridItemFitAnywhere(
            movingGridItem = movingGridItem,
            existingGridItems = gridRepository.gridItems.first(),
            page = page,
            gridRows = userData.rows,
            gridCols = userData.columns,
        )

        val isGridItemWithinBounds = isGridItemWithinBounds(
            gridItem = gridItem,
            gridRows = userData.rows,
            gridCols = userData.columns,
        )

        if (canGridItemFitAnywhere.not() || isGridItemWithinBounds.not()) {
            return
        }

        val updatedGridItems = gridRepository.gridItems.first().toMutableList().apply {
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