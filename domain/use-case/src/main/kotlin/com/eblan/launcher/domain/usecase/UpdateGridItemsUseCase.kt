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

class UpdateGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
) {
    suspend operator fun invoke(gridItems: List<GridItem>) {
        withContext(Dispatchers.Default) {
            val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val widgetGridItems = mutableListOf<WidgetGridItem>()

            val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val folderGridItems = mutableListOf<FolderGridItem>()

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
                                gridItemSettings = gridItem.gridItemSettings,
                            ),
                        )
                    }

                    is GridItemData.Folder -> {
                        folderGridItems.add(
                            FolderGridItem(
                                id = gridItem.id,
                                folderId = gridItem.folderId,
                                page = gridItem.page,
                                startRow = gridItem.startRow,
                                startColumn = gridItem.startColumn,
                                rowSpan = gridItem.rowSpan,
                                columnSpan = gridItem.columnSpan,
                                associate = gridItem.associate,
                                label = data.label,
                                gridItemSettings = gridItem.gridItemSettings,
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
                                gridItemSettings = gridItem.gridItemSettings,
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
                                gridItemSettings = gridItem.gridItemSettings,
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