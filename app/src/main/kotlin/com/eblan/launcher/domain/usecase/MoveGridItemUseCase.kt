package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemEdgeState
import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class MoveGridItemUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItemPixel: GridItemPixel?
    ): GridItem? {
        if (gridItemPixel == null) return null

        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val edgeState = getGridItemEdgeState(
                x = x,
                boundingBoxWidth = gridItemPixel.boundingBox.width,
                screenWidth = screenWidth,
                margin = 0
            )

            val updatedGridItem = moveGridItemWithCoordinates(
                gridItem = gridItemPixel.gridItem,
                x = x,
                y = y,
                rows = userData.rows,
                columns = userData.columns,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )

            val gridItems = gridRepository.gridItems.first()

            if (updatedGridItem != null && updatedGridItem !in gridItems) {
                updatedGridItem.copy(page = page, edgeState = edgeState)
            } else {
                null
            }
        }
    }
}