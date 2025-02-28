package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val gridItemBoundaryUseCase: GridItemBoundaryUseCase,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        id: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItemBoundary? {
        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridItems = gridRepository.gridItems.first()

            val movingGridItem = gridItems.find { gridItem ->
                gridItem.id == id
            }?.let { gridItem ->
                moveGridItemWithCoordinates(
                    gridItem = gridItem,
                    x = x,
                    y = y,
                    rows = userData.rows,
                    columns = userData.columns,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                ).copy(page = page)
            }

            if (movingGridItem != null && movingGridItem !in gridItems) {
                aStarGridAlgorithmUseCase(movingGridItem = movingGridItem)
            }

            gridItemBoundaryUseCase(
                id = id,
                x = x,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )
        }
    }
}