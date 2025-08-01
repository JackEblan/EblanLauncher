package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddPinWidgetToHomeScreenUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        className: String,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
    ): GridItem? {
        return withContext(Dispatchers.Default) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val rows = homeSettings.rows

            val columns = homeSettings.columns

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val dockHeight = homeSettings.dockHeight

            val gridItems = gridCacheRepository.gridCacheItems.first()

            val previewInferred = File(
                fileManager.widgetsDirectory,
                className,
            ).absolutePath

            val id = Uuid.random().toHexString()

            val gridHeight = rootHeight - dockHeight

            val cellWidth = rootWidth / columns

            val cellHeight = gridHeight / rows

            val (checkedRowSpan, checkedColumnSpan) = getSpan(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                minHeight = minHeight,
                minWidth = minWidth,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
            )

            val (checkedMinWidth, checkedMinHeight) = getSize(
                columns = columns,
                gridHeight = gridHeight,
                gridWidth = rootWidth,
                minHeight = minHeight,
                minWidth = minWidth,
                rows = rows,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
            )

            val data = GridItemData.Widget(
                appWidgetId = 0,
                componentName = componentName,
                packageName = packageName,
                configure = configure,
                minWidth = checkedMinWidth,
                minHeight = checkedMinHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
                preview = previewInferred,
            )

            val gridItem = GridItem(
                id = id,
                page = initialPage,
                startRow = 0,
                startColumn = 0,
                rowSpan = checkedRowSpan,
                columnSpan = checkedColumnSpan,
                data = data,
                associate = Associate.Grid,
            )

            val newGridItem = findAvailableRegion(
                gridItems = gridItems,
                pageCount = pageCount,
                rows = rows,
                columns = columns,
                gridItem = gridItem,
            )

            if (newGridItem != null) {
                gridCacheRepository.insertGridItem(gridItem = newGridItem)
            }

            newGridItem
        }
    }

    private fun getSpan(
        cellWidth: Int,
        cellHeight: Int,
        minWidth: Int,
        minHeight: Int,
        targetCellWidth: Int,
        targetCellHeight: Int,
    ): Pair<Int, Int> {
        val rowSpan = if (targetCellHeight == 0) {
            (minHeight + cellHeight - 1) / cellHeight
        } else {
            targetCellHeight
        }

        val columnSpan = if (targetCellWidth == 0) {
            (minWidth + cellWidth - 1) / cellWidth
        } else {
            targetCellWidth
        }

        return rowSpan to columnSpan
    }

    private fun getSize(
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
        targetCellWidth: Int,
        targetCellHeight: Int,
        minWidth: Int,
        minHeight: Int,
    ): Pair<Int, Int> {
        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        val width = if (targetCellWidth > 0) {
            targetCellWidth * cellWidth
        } else {
            minWidth
        }

        val height = if (targetCellHeight > 0) {
            targetCellHeight * cellHeight
        } else {
            minHeight
        }

        return width to height
    }
}