package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.findOverlappingGridItems
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
            gridItems.groupBy { gridItem -> gridItem.page }.flatMap { (page, gridItemsByPage) ->
                    val overlappingGridItems = findOverlappingGridItems(gridItems = gridItemsByPage)

                    val folderGridItem = if (overlappingGridItems.isNotEmpty()) {
                        createFolderFromOverlappingGridItems(overlappingGridItems, page)
                    } else {
                        null
                    }

                    val remaining = gridItemsByPage.filterNot { it in overlappingGridItems }

                    if (folderGridItem != null) {
                        remaining + folderGridItem
                    } else {
                        remaining
                    }
                }
        }
    }

    private fun createFolderFromOverlappingGridItems(
        gridItems: List<GridItem>,
        page: Int,
    ): GridItem {
        val firstGridItem = gridItems.minBy { gridItem -> gridItem.zIndex }

        val zIndex = gridItems.maxOf { gridItem -> gridItem.zIndex }

        return GridItem(
            id = firstGridItem.id,
            page = page,
            startRow = firstGridItem.startRow,
            startColumn = firstGridItem.startColumn,
            rowSpan = firstGridItem.rowSpan,
            columnSpan = firstGridItem.columnSpan,
            data = GridItemData.Folder(
                label = "Folder",
                gridItems = gridItems,
            ),
            associate = firstGridItem.associate,
            zIndex = zIndex,
        )
    }
}