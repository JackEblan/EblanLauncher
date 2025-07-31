package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default) {
            val gridCacheItems = gridCacheRepository.gridCacheItems.first()

            val applicationInfoGridItems = gridCacheItems.mapNotNull { gridItem ->
                val data = gridItem.data

                if (data is GridItemData.ApplicationInfo) {
                    toApplicationInfoGridItem(
                        id = gridItem.id,
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
                    )
                } else {
                    null
                }
            }

            val widgetGridItems = gridCacheItems.mapNotNull { gridItem ->
                val data = gridItem.data

                if (data is GridItemData.Widget) {
                    toWidgetGridItem(
                        id = gridItem.id,
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
                    )
                } else {
                    null
                }
            }

            val shortcutInfoGridItems = gridCacheItems.mapNotNull { gridItem ->
                val data = gridItem.data

                if (data is GridItemData.ShortcutInfo) {
                    toShortcutInfoGridItem(
                        id = gridItem.id,
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
                    )
                } else {
                    null
                }
            }

            applicationInfoGridItemRepository.upsertApplicationInfoGridItems(
                applicationInfoGridItems = applicationInfoGridItems,
            )

            widgetGridItemRepository.upsertWidgetGridItems(widgetGridItems = widgetGridItems)

            shortcutInfoGridItemRepository.upsertShortcutInfoGridItems(shortcutInfoGridItems = shortcutInfoGridItems)
        }
    }

    private fun toApplicationInfoGridItem(
        id: String,
        page: Int,
        startRow: Int,
        startColumn: Int,
        rowSpan: Int,
        columnSpan: Int,
        associate: Associate,
        componentName: String?,
        packageName: String,
        icon: String?,
        label: String?,
    ): ApplicationInfoGridItem {
        return ApplicationInfoGridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            componentName = componentName,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private fun toWidgetGridItem(
        id: String,
        page: Int,
        startRow: Int,
        startColumn: Int,
        rowSpan: Int,
        columnSpan: Int,
        associate: Associate,
        appWidgetId: Int,
        packageName: String,
        componentName: String,
        configure: String?,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        targetCellHeight: Int,
        targetCellWidth: Int,
        preview: String?,
    ): WidgetGridItem {
        return WidgetGridItem(
            id = id,
            page = page,
            startRow = startRow,
            startColumn = startColumn,
            rowSpan = rowSpan,
            columnSpan = columnSpan,
            associate = associate,
            appWidgetId = appWidgetId,
            packageName = packageName,
            componentName = componentName,
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
        )
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