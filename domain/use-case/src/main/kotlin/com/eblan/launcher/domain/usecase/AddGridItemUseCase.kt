package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddGridItemUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): Int {
        val userData = userDataRepository.userData.first()

        val (startRow, startColumn) = coordinatesToStartPosition(
            x = x,
            y = y,
            rows = userData.rows,
            columns = userData.columns,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )

        val gridItem = GridItem(
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
        )

        val gridItemId = gridRepository.upsertGridItem(gridItem = gridItem).toInt()

        val movingGridItem = gridRepository.getGridItem(id = gridItemId)

        return if (movingGridItem != null) {
            aStarGridAlgorithmUseCase(movingGridItem = movingGridItem)

            movingGridItem.id
        } else {
            -1
        }
    }
}