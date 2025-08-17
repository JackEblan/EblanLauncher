package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
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
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
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
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val rows = homeSettings.rows

            val columns = homeSettings.columns

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val dockHeight = homeSettings.dockHeight

            val gridItems = gridCacheRepository.gridCacheItems.first()

            val previewInferred = File(
                fileManager.getDirectory(FileManager.WIDGETS_DIR),
                className,
            ).absolutePath

            val gridHeight = rootHeight - dockHeight

            val cellWidth = rootWidth / columns

            val cellHeight = gridHeight / rows

            val (checkedRowSpan, checkedColumnSpan) = getWidgetGridItemSpan(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                minHeight = minHeight,
                minWidth = minWidth,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
            )

            val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
                rows = rows,
                columns = columns,
                gridWidth = rootWidth,
                gridHeight = gridHeight,
                minWidth = minWidth,
                minHeight = minHeight,
                targetCellWidth = targetCellWidth,
                targetCellHeight = targetCellHeight,
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
                id = Uuid.random().toHexString(),
                folderId = null,
                page = initialPage,
                startRow = 0,
                startColumn = 0,
                rowSpan = checkedRowSpan,
                columnSpan = checkedColumnSpan,
                data = data,
                associate = Associate.Grid,
                override = false,
                gridItemSettings = homeSettings.gridItemSettings,
            )

            val newGridItem = findAvailableRegionByPage(
                gridItems = gridItems,
                gridItem = gridItem,
                pageCount = pageCount,
                rows = rows,
                columns = columns,
            )

            if (newGridItem != null) {
                gridCacheRepository.insertGridItem(gridItem = newGridItem)
            }

            newGridItem
        }
    }
}