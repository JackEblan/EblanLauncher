package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class AddApplicationInfoUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        screenWidth: Int,
        screenHeight: Int,
        data: GridItemData,
    ): GridItem {
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
            id = UUID.randomUUID().toString(),
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = data,
        )

        return gridItem
    }
}