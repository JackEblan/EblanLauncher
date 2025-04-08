package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemDimensions
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class AddAppWidgetProviderInfoUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
) {
    suspend operator fun invoke(
        page: Int,
        componentName: String,
        x: Int,
        y: Int,
        rowSpan: Int,
        columnSpan: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        screenWidth: Int,
        screenHeight: Int,
    ): GridItemDimensions {
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

        val newWidth = if (columnSpan == 0) {
            minWidth
        } else {
            columnSpan * cellWidth
        }

        val newHeight = if (rowSpan == 0) {
            minHeight
        } else {
            rowSpan * cellHeight
        }

        val (startRow, startColumn) = coordinatesToStartPosition(
            x = x,
            y = y,
            rows = userData.rows,
            columns = userData.columns,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )

        val data = GridItemData.Widget(
            appWidgetId = -1,
            componentName = componentName,
            width = newWidth,
            height = newHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
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

        return GridItemDimensions(
            gridItem = gridItem,
            width = gridItem.columnSpan * cellWidth,
            height = gridItem.rowSpan * cellHeight,
            x = gridItem.startColumn * cellWidth,
            y = gridItem.startRow * cellHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
        )
    }
}