package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MoveGridItemUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItemPixel: GridItemPixel?
    ) {
        if (gridItemPixel == null) return

        withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val updatedGridItem = moveGridItemWithCoordinates(
                gridItem = gridItemPixel.gridItem,
                x = x,
                y = y,
                gridWidth = userData.rows,
                gridHeight = userData.columns,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
                boundingBoxWidth = gridItemPixel.boundingBox.width,
                boundingBoxHeight = gridItemPixel.boundingBox.height
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