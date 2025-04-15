package com.eblan.launcher.feature.home.screen.widget

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
fun rememberWidgetState(): WidgetState {
    return remember {
        WidgetState()
    }
}

@Stable
class WidgetState {
    @OptIn(ExperimentalUuidApi::class)
    fun getGridItemLayoutInfo(
        page: Int,
        componentName: String,
        rows: Int,
        columns: Int,
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
        screenSize: IntSize,
    ): GridItemLayoutInfo {
        val cellWidth = screenSize.width / columns

        val cellHeight = screenSize.height / rows

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
            rows = rows,
            columns = columns,
            screenWidth = screenSize.width,
            screenHeight = screenSize.height,
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
            id = Uuid.random().toHexString(),
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = newRowSpan,
            columnSpan = newColumnSpan,
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