package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
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
                updatedGridItem.copy(page = page)
            } else {
                null
            }
        }
    }
}