package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ResizeGridItemUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        newPixelWidth: Int,
        newPixelHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItem: GridItem?,
    ) {
        if (gridItem == null) return

        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val updatedGridItem = resizeGridItemWithPixels(
                gridItem = gridItem,
                newPixelWidth = newPixelWidth,
                newPixelHeight = newPixelHeight,
                gridCellPixelWidth = screenWidth / userData.rows,
                gridCellPixelHeight = screenHeight / userData.columns
            )

            val gridItems = gridRepository.gridItems.first()

            if (updatedGridItem != null && updatedGridItem !in gridItems) {
                aStarGridAlgorithmUseCase(
                    page = page, gridItem = updatedGridItem
                )
            }
        }
    }
}