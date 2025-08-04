package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.groupOverlappingGridItems
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val gridCacheRepository: GridCacheRepository,
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<GridItem>> {
        return gridCacheRepository.isCache.flatMapLatest { isCache ->
            if (isCache) {
                gridCacheRepository.gridCacheItems
            } else {
                combine(
                    applicationInfoGridItemRepository.applicationInfoGridItems,
                    widgetGridItemRepository.widgetGridItems,
                    shortcutInfoGridItemRepository.shortcutInfoGridItems,
                ) { applicationInfoGridItems, widgetGridItems, shortcutInfoGridItems ->
                    applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems
                }
            }
        }.map { gridItems ->
            gridItems
                .groupBy { it.page }
                .flatMap { (page, gridItemsByPage) ->
                    val overlappingGroups = groupOverlappingGridItems(gridItems = gridItemsByPage)

                    val folderGridItems = overlappingGroups.map { group ->
                        createFolderFromOverlappingGridItems(page, group)
                    }

                    val groupedItems = overlappingGroups.flatten().toSet()

                    val remaining = gridItemsByPage.filterNot { it in groupedItems }

                    remaining + folderGridItems
                }
        }
    }

    private fun createFolderFromOverlappingGridItems(
        page: Int,
        gridItems: List<GridItem>,
    ): GridItem {
        val firstGridItem = gridItems.minBy { gridItem -> gridItem.zIndex }

        return GridItem(
            id = firstGridItem.id,
            page = page,
            startRow = firstGridItem.startRow,
            startColumn = firstGridItem.startColumn,
            rowSpan = firstGridItem.rowSpan,
            columnSpan = firstGridItem.columnSpan,
            data = GridItemData.Folder(
                label = "Unknown",
                gridItems = gridItems,
            ),
            associate = firstGridItem.associate,
            zIndex = firstGridItem.zIndex,
        )
    }
}