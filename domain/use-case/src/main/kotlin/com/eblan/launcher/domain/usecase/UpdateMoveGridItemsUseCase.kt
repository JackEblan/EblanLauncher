package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
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

class UpdateMoveGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
) {
    suspend operator fun invoke(
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ) {
        withContext(Dispatchers.Default) {
            val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val widgetGridItems = mutableListOf<WidgetGridItem>()

            val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val folderGridItems = mutableListOf<FolderGridItem>()

            // Find the last conflicting item
            val gridItemByCoordinates = getGridItemByCoordinates(
                id = movingGridItem.id,
                gridItems = gridItems,
                rows = rows,
                columns = columns,
                x = x,
                y = y,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            if (gridItemByCoordinates != null) {
                when (val data = gridItemByCoordinates.data) {
                    is GridItemData.Folder -> {
                        // Add the moving grid item in the folder
                        val newData = data.copy(
                            gridItems = data.gridItems + movingGridItem,
                        )

                        val folderIndex =
                            gridItems.indexOfFirst { it.id == gridItemByCoordinates.id }

                        gridItems[folderIndex] = gridItemByCoordinates.copy(data = newData)
                    }

                    else -> {
                        // We use the conflicting grid item to host folder
                        val conflictingIndex =
                            gridItems.indexOfFirst { it.id == gridItemByCoordinates.id }

                        val newData = GridItemData.Folder(
                            label = "Unknown",
                            gridItems = listOf(
                                gridItemByCoordinates,
                                movingGridItem,
                            ),
                        )

                        gridItems[conflictingIndex] = gridItemByCoordinates.copy(data = newData)
                    }
                }
            }

            gridItems.map { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        applicationInfoGridItems.add(
                            toApplicationInfoGridItem(
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
                            toWidgetGridItem(
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
                            toShortcutInfoGridItem(
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

    private fun toApplicationInfoGridItem(
        id: String,
        folderId: String?,
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
            folderId = folderId,
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
        folderId: String?,
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
            folderId = folderId,
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
        folderId: String?,
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
            folderId = folderId,
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