package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

class GetGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val gridCacheRepository: GridCacheRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<GridItem>> {
        return gridCacheRepository.isCache.flatMapLatest { isCache ->
            if (isCache) {
                gridCacheRepository.gridCacheItems
            } else {
                combine(
                    applicationInfoGridItemRepository.applicationInfoGridItems,
                    widgetGridItemRepository.widgetGridItems,
                    shortcutInfoGridItemRepository.shortcutInfoGridItems,
                    folderGridItemRepository.folderGridItems,
                ) { applicationInfoGridItems, widgetGridItems, shortcutInfoGridItems, folderGridItems ->
                    (applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems + folderGridItems)
                        .filterNot { gridItem ->
                            gridItem.folderId != null
                        }
                }
            }
        }
    }
}