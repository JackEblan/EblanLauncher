package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.grid.moveGridItemWithCoordinates
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class AddAppWidgetProviderInfoUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val aStarGridAlgorithmUseCase: AStarGridAlgorithmUseCase,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ): GridItem {
        val userData = userDataRepository.userData.first()

        val cellWidth = screenWidth / userData.columns

        val cellHeight = screenHeight / userData.rows

        val newRowSpan = if (rowSpan == 0) {
            (minHeight + cellHeight - 1) / cellHeight
        } else {
            rowSpan
        }

        val newColumnSpan = if (columnSpan == 0) {
            (minWidth + cellWidth - 1) / cellWidth
        } else {
            columnSpan
        }

        val (startRow, startColumn) = coordinatesToStartPosition(
            x = x,
            y = y,
            rows = userData.rows,
            columns = userData.columns,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )

        val gridItem = GridItem(
            id = UUID.randomUUID().toString(),
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = newRowSpan,
            columnSpan = newColumnSpan,
            data = data,
        )

        val movingGridItem = moveGridItemWithCoordinates(
            gridItem = gridItem,
            x = x,
            y = y,
            rows = userData.rows,
            columns = userData.columns,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        ).copy(page = page)

        aStarGridAlgorithmUseCase(gridItem = movingGridItem)

        return gridItem
    }
}