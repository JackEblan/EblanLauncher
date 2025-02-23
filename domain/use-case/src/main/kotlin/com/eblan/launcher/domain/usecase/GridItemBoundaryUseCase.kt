package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.calculateBoundingBox
import com.eblan.launcher.domain.grid.getGridItemBoundary
import com.eblan.launcher.domain.model.GridItemBoundary
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GridItemBoundaryUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        id: Int,
        x: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItemBoundary? {
        return withContext(Dispatchers.Default) {
            val userData = userDataRepository.userData.first()

            val gridItems = gridRepository.gridItems.first()

            gridItems.find { gridItem ->
                gridItem.id == id
            }?.let { gridItem ->
                val boundingBox = calculateBoundingBox(
                    gridCells = gridItem.cells,
                    rows = userData.rows,
                    columns = userData.columns,
                    screenWidth = screenWidth,
                    screenHeight = screenHeight,
                )

                getGridItemBoundary(
                    x = x,
                    boundingBoxWidth = boundingBox.width,
                    screenWidth = screenWidth,
                )
            }
        }
    }
}