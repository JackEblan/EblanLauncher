package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetGridItemByCoordinatesUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItemOverlay? {
        val userData = userDataRepository.userData.first()

        val cellWidth = screenWidth / userData.columns

        val cellHeight = screenHeight / userData.rows

        val gridItem = gridRepository.gridItems.first().find { gridItem ->
            val startRow = y / cellHeight

            val startColumn = x / cellWidth

            val rowInSpan =
                startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

            val columnInSpan =
                startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

            gridItem.page == page && rowInSpan && columnInSpan
        }

        return if (gridItem != null) {
            val gridItemWidth = gridItem.columnSpan * cellWidth

            val gridItemHeight = gridItem.rowSpan * cellHeight

            val gridItemX = gridItem.startColumn * cellWidth

            val gridItemY = gridItem.startRow * cellHeight

            GridItemOverlay(
                id = gridItem.id,
                width = gridItemWidth,
                height = gridItemHeight,
                x = gridItemX,
                y = gridItemY,
                screenWidth = screenWidth,
                screenHeight = screenHeight,
            )
        } else {
            null
        }
    }
}