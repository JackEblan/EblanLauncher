package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
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
    ): GridItem? {
        val userData = userDataRepository.userData.first()

        val cellWidth = screenWidth / userData.rows

        val cellHeight = screenHeight / userData.columns

        return gridRepository.gridItems.first().find { gridItem ->
            val startRow = y / cellHeight

            val startColumn = x / cellWidth

            val rowInSpan =
                startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

            val columnInSpan =
                startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

            gridItem.page == page && rowInSpan && columnInSpan
        }
    }
}