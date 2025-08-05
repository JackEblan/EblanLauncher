package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
) {
    suspend operator fun invoke(
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) {
        withContext(Dispatchers.Default) {
            val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val widgetGridItems = mutableListOf<WidgetGridItem>()

            val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val folderGridItems = mutableListOf<FolderGridItem>()

            if (conflictingGridItem != null) {
                val movingIndex =
                    gridItems.indexOfFirst { it.id == movingGridItem.id }

                val conflictingIndex =
                    gridItems.indexOfFirst { it.id == conflictingGridItem.id }

                when (conflictingGridItem.data) {
                    is GridItemData.Folder -> {
                        gridItems[movingIndex] =
                            movingGridItem.copy(folderId = conflictingGridItem.id)
                    }

                    else -> {
                        val folderId = conflictingGridItem.id

                        val conflictingGridItemWithNewFolderId =
                            conflictingGridItem.copy(folderId = folderId)

                        val movingGridItemWithNewFolderId = movingGridItem.copy(folderId = folderId)

                        val newData = GridItemData.Folder(
                            label = "Unknown",
                            gridItems = listOf(
                                movingGridItemWithNewFolderId,
                                conflictingGridItemWithNewFolderId,
                            ),
                        )

                        gridItems[conflictingIndex] = conflictingGridItemWithNewFolderId

                        gridItems[movingIndex] = movingGridItemWithNewFolderId

                        gridItems.add(conflictingGridItemWithNewFolderId.copy(data = newData))
                    }
                }
            }

            gridItems.map { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        applicationInfoGridItems.add(
                            ApplicationInfoGridItem(
                                id = gridItem.id,
                                folderId = gridItem.folderId,
                                page = gridItem.page,
                                startRow = gridItem.startRow,
                                startColumn = gridItem.startColumn,
                                rowSpan = gridItem.rowSpan,
                                columnSpan = gridItem.columnSpan,
                                associate = gridItem.associate,
                                componentName = data.componentName,
                                packageName = data.packageName,
                                icon = data.icon,
                                label = data.label,
                            ),
                        )
                    }

                    is GridItemData.Folder -> {
                        folderGridItems.add(
                            FolderGridItem(
                                id = gridItem.id,
                                page = gridItem.page,
                                startRow = gridItem.startRow,
                                startColumn = gridItem.startColumn,
                                rowSpan = gridItem.rowSpan,
                                columnSpan = gridItem.columnSpan,
                                associate = gridItem.associate,
                                label = data.label,
                            ),
                        )
                    }

                    is GridItemData.Widget -> {
                        widgetGridItems.add(
                            WidgetGridItem(
                                id = gridItem.id,
                                folderId = gridItem.folderId,
                                page = gridItem.page,
                                startRow = gridItem.startRow,
                                startColumn = gridItem.startColumn,
                                rowSpan = gridItem.rowSpan,
                                columnSpan = gridItem.columnSpan,
                                associate = gridItem.associate,
                                appWidgetId = data.appWidgetId,
                                packageName = data.packageName,
                                componentName = data.componentName,
                                configure = data.configure,
                                minWidth = data.minWidth,
                                minHeight = data.minHeight,
                                resizeMode = data.resizeMode,
                                minResizeWidth = data.minResizeWidth,
                                minResizeHeight = data.minResizeHeight,
                                maxResizeWidth = data.maxResizeWidth,
                                maxResizeHeight = data.maxResizeHeight,
                                targetCellHeight = data.targetCellHeight,
                                targetCellWidth = data.targetCellWidth,
                                preview = data.preview,
                            ),
                        )
                    }

                    is GridItemData.ShortcutInfo -> {
                        shortcutInfoGridItems.add(
                            ShortcutInfoGridItem(
                                id = gridItem.id,
                                folderId = gridItem.folderId,
                                page = gridItem.page,
                                startRow = gridItem.startRow,
                                startColumn = gridItem.startColumn,
                                rowSpan = gridItem.rowSpan,
                                columnSpan = gridItem.columnSpan,
                                associate = gridItem.associate,
                                shortcutId = data.shortcutId,
                                packageName = data.packageName,
                                shortLabel = data.shortLabel,
                                longLabel = data.longLabel,
                                icon = data.icon,
                            ),
                        )
                    }
                }
            }

            applicationInfoGridItemRepository.upsertApplicationInfoGridItems(
                applicationInfoGridItems = applicationInfoGridItems,
            )

            widgetGridItemRepository.upsertWidgetGridItems(widgetGridItems = widgetGridItems)

            shortcutInfoGridItemRepository.upsertShortcutInfoGridItems(shortcutInfoGridItems = shortcutInfoGridItems)

            folderGridItemRepository.upsertFolderGridItems(folderGridItems = folderGridItems)
        }
    }
}