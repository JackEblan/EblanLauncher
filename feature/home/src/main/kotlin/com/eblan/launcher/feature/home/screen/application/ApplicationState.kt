package com.eblan.launcher.feature.home.screen.application

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import com.eblan.launcher.domain.grid.coordinatesToStartPosition
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.GridItemLayoutInfo
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun rememberApplicationState(): ApplicationState {
    return remember {
        ApplicationState()
    }
}

@Stable
class ApplicationState {
    @OptIn(ExperimentalUuidApi::class)
    fun getGridItemLayoutInfo(
        page: Int,
        rows: Int,
        columns: Int,
        x: Int,
        y: Int,
        screenSize: IntSize,
        data: GridItemData,
    ): GridItemLayoutInfo {
        val cellWidth = screenSize.width / columns

        val cellHeight = screenSize.height / rows

        val (startRow, startColumn) = coordinatesToStartPosition(
            x = x,
            y = y,
            rows = rows,
            columns = columns,
            screenWidth = screenSize.width,
            screenHeight = screenSize.height,
        )

        val gridItem = GridItem(
            id = Uuid.random().toHexString(),
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = 1,
            columnSpan = 1,
            data = data,
        )

        return GridItemLayoutInfo(
            gridItem = gridItem,
            width = gridItem.columnSpan * cellWidth,
            height = gridItem.rowSpan * cellHeight,
            x = gridItem.startColumn * cellWidth,
            y = gridItem.startRow * cellHeight,
            screenWidth = screenSize.width,
            screenHeight = screenSize.height,
        )
    }
}
