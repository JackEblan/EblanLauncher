package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetGridItemByCoordinates @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        x: Int, y: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItem? {
        val userData = userDataRepository.userData.first()

        val cellWidth = screenWidth / userData.columns

        val cellHeight = screenHeight / userData.rows

        return gridRepository.gridItems.first().find { gridItem ->
            val gridItemX = gridItem.startColumn * cellWidth

            val gridItemY = gridItem.startRow * cellHeight

            val startRow = (gridItemY - y) / cellHeight

            val startColumn = (gridItemX - x) / cellWidth

            gridItem.startRow == startRow && gridItem.startColumn == startColumn
        }
    }
}