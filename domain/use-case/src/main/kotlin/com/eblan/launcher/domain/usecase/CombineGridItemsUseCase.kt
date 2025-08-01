package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class CombineGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val gridCacheRepository: GridCacheRepository,
) {
    operator fun invoke(): Flow<List<GridItem>> {
        return combine(
            applicationInfoGridItemRepository.applicationInfoGridItems,
            widgetGridItemRepository.widgetGridItems,
            shortcutInfoGridItemRepository.shortcutInfoGridItems,
        ) { applicationInfoGridItems, widgetGridItems, shortcutInfoGridItems ->
            val gridItems =
                applicationInfoGridItems + widgetGridItems + shortcutInfoGridItems

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            gridItems
        }
    }
}