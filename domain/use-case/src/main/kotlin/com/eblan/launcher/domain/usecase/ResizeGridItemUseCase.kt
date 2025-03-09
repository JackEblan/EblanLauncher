package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.resizeGridItemWithPixels
import com.eblan.launcher.domain.model.Anchor
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        id: Int,
        width: Int,
        height: Int,
        cellWidth: Int,
        cellHeight: Int,
        anchor: Anchor,
    ) {
        withContext(Dispatchers.Default) {
            val gridItems = gridRepository.gridItems.first()

            val movingGridItem = gridItems.find { gridItem ->
                gridItem.id == id
            }?.let { gridItem ->
                resizeGridItemWithPixels(
                    gridItem = gridItem,
                    width = width,
                    height = height,
                    gridCellWidth = cellWidth,
                    gridCellHeight = cellHeight,
                    anchor = anchor,
                ).copy(page = page)
            }

            if (movingGridItem != null && movingGridItem !in gridItems) {
                aStarGridAlgorithmUseCase(movingGridItem = movingGridItem)
            }
        }
    }
}