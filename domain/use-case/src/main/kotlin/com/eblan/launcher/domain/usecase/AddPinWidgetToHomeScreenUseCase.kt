package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.grid.getWidgetGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AddPinWidgetToHomeScreenUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
) {
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

            val gridItems = gridRepository.gridItems.first()

            val previewInferred =
                File(
                    fileManager.widgetsDirectory,
                    className,
                ).absolutePath

            val gridItem = getWidgetGridItem(
                page = initialPage,
                rows = rows,
                columns = columns,
                componentName = componentName,
                configure = configure,
                packageName = packageName,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                preview = previewInferred,
                gridWidth = rootWidth,
                gridHeight = rootHeight - dockHeight,
            )

            val newGridItem = findAvailableRegion(
                gridItems = gridItems,
                pageCount = pageCount,
                rows = rows,
                columns = columns,
                gridItem = gridItem,
            )

            if (newGridItem != null) {
                gridRepository.upsertGridItem(gridItem = newGridItem)
            }

            newGridItem
        }
    }
}