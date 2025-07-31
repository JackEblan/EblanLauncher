package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetGridItemsUseCase @Inject constructor(
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
            val newApplicationInfoGridItems =
                applicationInfoGridItems.map { applicationInfoGridItem ->
                    applicationInfoGridItem.toGridItem()
                }

            val newWidgetGridItems = widgetGridItems.map { widgetGridItem ->
                widgetGridItem.toGridItem()
            }

            val newShortcutInfoGridItems = shortcutInfoGridItems.map { shortcutInfoGridItem ->
                shortcutInfoGridItem.toGridItem()
            }

            val gridItems =
                newApplicationInfoGridItems + newWidgetGridItems + newShortcutInfoGridItems

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            gridItems
        }
    }

    private fun ApplicationInfoGridItem.toGridItem(): GridItem {
        return GridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = GridItemData.ApplicationInfo(
                componentName = componentName,
                packageName = packageName,
                icon = icon,
                label = label,
            ),
            associate = associate,
        )
    }

    private fun WidgetGridItem.toGridItem(): GridItem {
        return GridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = GridItemData.Widget(
                appWidgetId = appWidgetId,
                componentName = componentName,
                packageName = packageName,
                configure = configure,
                minWidth = minWidth,
                minHeight = minHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
                preview = preview,
            ),
            associate = associate,
        )
    }

    private fun ShortcutInfoGridItem.toGridItem(): GridItem {
        return GridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            data = GridItemData.ShortcutInfo(
                shortcutId = shortcutId,
                packageName = packageName,
                shortLabel = shortLabel,
                longLabel = longLabel,
                icon = icon,
            ),
            associate = associate,
        )
    }
}