package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.repository.GridRepository
import com.eblan.launcher.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ResizeGridItemUseCase(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        width: Int,
        height: Int,
        screenWidth: Int,
        screenHeight: Int,
        gridItem: GridItem?,
        anchor: Anchor,
    ): GridItem? {
        if (gridItem == null) return null

        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val updatedGridItem = resizeGridItemWithPixels(
                gridItem = gridItem,
                width = width,
                height = height,
                gridCellWidth = screenWidth / userData.rows,
                gridCellHeight = screenHeight / userData.columns,
                anchor = anchor,
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