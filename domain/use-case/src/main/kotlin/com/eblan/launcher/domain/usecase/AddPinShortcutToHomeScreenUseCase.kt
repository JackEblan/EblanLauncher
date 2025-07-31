package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
) {
    suspend operator fun invoke(
        shortcutId: String,
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
                page = initialPage,
                startRow = 0,
                startColumn = 0,
                rowSpan = 1,
                columnSpan = 1,
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
                val shortcutInfoGridItem = toShortcutInfoGridItem(
                    id = shortcutId,
                    page = newGridItem.page,
                    startRow = newGridItem.startRow,
                    startColumn = newGridItem.startColumn,
                    rowSpan = newGridItem.rowSpan,
                    columnSpan = newGridItem.columnSpan,
                    associate = newGridItem.associate,
                    shortcutId = shortcutId,
                    packageName = packageName,
                    shortLabel = shortLabel,
                    longLabel = longLabel,
                    icon = icon,
                )

                shortcutInfoGridItemRepository.upsertShortcutInfoGridItem(shortcutInfoGridItem = shortcutInfoGridItem)
            }

            newGridItem
        }
    }

    private fun toShortcutInfoGridItem(
        id: String,
        page: Int,
        startRow: Int,
        startColumn: Int,
        rowSpan: Int,
        columnSpan: Int,
        associate: Associate,
        shortcutId: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        icon: String?,
    ): ShortcutInfoGridItem {
        return ShortcutInfoGridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            shortcutId = shortcutId,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
        )
    }
}