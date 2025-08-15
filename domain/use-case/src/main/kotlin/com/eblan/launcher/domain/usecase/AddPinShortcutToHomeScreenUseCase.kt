package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        shortcutId: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray,
    ): GridItem? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val rows = homeSettings.rows

            val columns = homeSettings.columns

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val gridItems = gridCacheRepository.gridCacheItems.first()

            val icon = fileManager.writeFileBytes(
                directory = fileManager.shortcutsDirectory,
                name = shortcutId,
                byteArray = byteArray,
            )

            val data = GridItemData.ShortcutInfo(
                shortcutId = shortcutId,
                packageName = packageName,
                shortLabel = shortLabel,
                longLabel = longLabel,
                icon = icon,
            )

            val gridItem = GridItem(
                id = shortcutId,
                folderId = null,
                page = initialPage,
                startRow = 0,
                startColumn = 0,
                rowSpan = 1,
                columnSpan = 1,
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