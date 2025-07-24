package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
) {
    suspend operator fun invoke(
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray,
    ): GridItem? {
        return withContext(Dispatchers.Default) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val rows = homeSettings.rows

            val columns = homeSettings.columns

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val gridItems = gridRepository.gridItems.first()

            val icon = fileManager.writeFileBytes(
                directory = fileManager.shortcutsDirectory,
                name = id,
                byteArray = byteArray,
            )

            val newGridItem = findAvailableRegion(
                gridItems = gridItems,
                pageCount = pageCount,
                rows = rows,
                columns = columns,
                gridItem = getShortcutGridItem(
                    page = initialPage,
                    id = id,
                    packageName = packageName,
                    shortLabel = shortLabel,
                    longLabel = longLabel,
                    icon = icon,
                ),
            )

            if (newGridItem != null) {
                gridRepository.upsertGridItem(gridItem = newGridItem)
            }

            newGridItem
        }
    }

    private fun getShortcutGridItem(
        page: Int,
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        icon: String?,
    ): GridItem {
        val data = GridItemData.ShortcutInfo(
            id = id,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
        )

        return GridItem(
            id = 0,
            page = page,
            startRow = 0,
            startColumn = 0,
            rowSpan = 1,
            columnSpan = 1,
            dataId = data.packageName,
            data = data,
            associate = Associate.Grid,
        )
    }
}