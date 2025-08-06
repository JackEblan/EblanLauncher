package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
) {
    suspend operator fun invoke(
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) {
        withContext(Dispatchers.Default) {
            pageItemsToDelete.forEach { pageItem ->
                gridCacheRepository.deleteGridItems(gridItems = pageItem.gridItems)

                pageItem.gridItems.forEach { gridItem ->
                    val data = gridItem.data

                    if (data is GridItemData.Widget) {
                        appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)
                    }
                }
            }

            val gridItems = pageItems.mapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }.flatten()

            val newInitialPage = pageItems.indexOfFirst { pageItem -> pageItem.id == initialPage }

            if (initialPage != -1) {
                userDataRepository.updateInitialPage(initialPage = newInitialPage)
            }

            userDataRepository.updatePageCount(pageCount = pageItems.size)

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