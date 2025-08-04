package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val getGridItemsUseCase: GetGridItemsUseCase,
) {
    suspend operator fun invoke() {
        withContext(Dispatchers.Default) {
            val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val widgetGridItems = mutableListOf<WidgetGridItem>()

            val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            getGridItemsUseCase().first().map { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        applicationInfoGridItems.add(
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
                                zIndex = gridItem.zIndex,
                            ),
                        )
                    }

                    is GridItemData.Folder -> {
                        addFolderGridItems(
                            startRow = gridItem.startRow,
                            startColumn = gridItem.startColumn,
                            folderGridItems = data.gridItems,
                            applicationInfoGridItems = applicationInfoGridItems,
                            shortcutInfoGridItems = shortcutInfoGridItems,
                            widgetGridItems = widgetGridItems,
                        )
                    }

                    is GridItemData.Widget -> {
                        widgetGridItems.add(
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
                                zIndex = gridItem.zIndex,
                            ),
                        )
                    }

                    is GridItemData.ShortcutInfo -> {
                        shortcutInfoGridItems.add(
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
                                zIndex = gridItem.zIndex,
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
        }
    }

    private fun addFolderGridItems(
        startRow: Int,
        startColumn: Int,
        folderGridItems: List<GridItem>,
        applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
        shortcutInfoGridItems: MutableList<ShortcutInfoGridItem>,
        widgetGridItems: MutableList<WidgetGridItem>,
    ) {
        folderGridItems.forEach { folderGridItem ->
            when (val folderData = folderGridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItems.add(
                        toApplicationInfoGridItem(
                            id = folderGridItem.id,
                            page = folderGridItem.page,
                            startRow = startRow,
                            startColumn = startColumn,
                            rowSpan = folderGridItem.rowSpan,
                            columnSpan = folderGridItem.columnSpan,
                            associate = folderGridItem.associate,
                            componentName = folderData.componentName,
                            packageName = folderData.packageName,
                            icon = folderData.icon,
                            label = folderData.label,
                            zIndex = folderGridItem.zIndex,
                        ),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItems.add(
                        toShortcutInfoGridItem(
                            id = folderGridItem.id,
                            page = folderGridItem.page,
                            startRow = startRow,
                            startColumn = startColumn,
                            rowSpan = folderGridItem.rowSpan,
                            columnSpan = folderGridItem.columnSpan,
                            associate = folderGridItem.associate,
                            shortcutId = folderData.shortcutId,
                            packageName = folderData.packageName,
                            shortLabel = folderData.shortLabel,
                            longLabel = folderData.longLabel,
                            icon = folderData.icon,
                            zIndex = folderGridItem.zIndex,
                        ),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItems.add(
                        toWidgetGridItem(
                            id = folderGridItem.id,
                            page = folderGridItem.page,
                            startRow = startRow,
                            startColumn = startColumn,
                            rowSpan = folderGridItem.rowSpan,
                            columnSpan = folderGridItem.columnSpan,
                            associate = folderGridItem.associate,
                            appWidgetId = folderData.appWidgetId,
                            packageName = folderData.packageName,
                            componentName = folderData.componentName,
                            configure = folderData.configure,
                            minWidth = folderData.minWidth,
                            minHeight = folderData.minHeight,
                            resizeMode = folderData.resizeMode,
                            minResizeWidth = folderData.minResizeWidth,
                            minResizeHeight = folderData.minResizeHeight,
                            maxResizeWidth = folderData.maxResizeWidth,
                            maxResizeHeight = folderData.maxResizeHeight,
                            targetCellHeight = folderData.targetCellHeight,
                            targetCellWidth = folderData.targetCellWidth,
                            preview = folderData.preview,
                            zIndex = folderGridItem.zIndex,
                        ),
                    )
                }

                is GridItemData.Folder -> Unit
            }
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
        zIndex: Int,
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
            zIndex = zIndex,
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
        zIndex: Int,
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
            zIndex = zIndex,
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
        zIndex: Int,
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
            zIndex = zIndex,
        )
    }
}