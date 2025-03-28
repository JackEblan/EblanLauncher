package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddApplicationInfoUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    @OptIn(ExperimentalUuidApi::class)
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
            id = Uuid.random().toHexString(),
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