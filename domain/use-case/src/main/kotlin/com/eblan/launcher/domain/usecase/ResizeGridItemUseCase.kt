package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        anchor: Anchor,
    ) {
        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridItems = gridRepository.gridItems.first()

            val movingGridItem = gridItems.find { gridItem ->
                gridItem.id == id
            }?.let { gridItem ->
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = height,
                    gridCellWidth = screenWidth / userData.rows,
                    gridCellHeight = screenHeight / userData.columns,
                    anchor = anchor,
                ).copy(page = page)
            }

            if (movingGridItem != null && movingGridItem !in gridItems) {
                aStarGridAlgorithmUseCase(movingGridItem = movingGridItem)
            }
        }
    }
}