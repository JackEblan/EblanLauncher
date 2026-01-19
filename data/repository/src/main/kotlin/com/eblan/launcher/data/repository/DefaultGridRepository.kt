package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asApplicationInfo
import com.eblan.launcher.data.repository.mapper.asFolderGridItem
import com.eblan.launcher.data.repository.mapper.asShortcutConfigGridItem
import com.eblan.launcher.data.repository.mapper.asShortcutInfoGridItem
import com.eblan.launcher.data.repository.mapper.asWidgetGridItem
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import java.io.File
import javax.inject.Inject

internal class DefaultGridRepository @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
) : GridRepository {
    override suspend fun updateGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.updateApplicationInfoGridItem(
                    applicationInfoGridItem = gridItem.asApplicationInfo(data = data),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.updateFolderGridItem(
                    folderGridItem = gridItem.asFolderGridItem(data = data),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.updateShortcutInfoGridItem(
                    shortcutInfoGridItem = gridItem.asShortcutInfoGridItem(data = data),
                )
            }

            is GridItemData.Widget -> {
                widgetGridItemRepository.updateWidgetGridItem(
                    widgetGridItem = gridItem.asWidgetGridItem(data = data),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.updateShortcutConfigGridItem(
                    shortcutConfigGridItem = gridItem.asShortcutConfigGridItem(data = data),
                )
            }
        }
    }

    override suspend fun restoreGridItem(gridItem: GridItem): GridItem {
        return when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                data.customIcon?.let { customIcon ->
                    val customIconFile = File(customIcon)

                    if (customIconFile.exists()) {
                        File(customIcon).delete()
                    }
                }

                val eblanApplicationInfo =
                    eblanApplicationInfoRepository.getEblanApplicationInfo(
                        serialNumber = data.serialNumber,
                        packageName = data.packageName,
                    )

                if (eblanApplicationInfo != null) {
                    eblanApplicationInfoRepository.updateEblanApplicationInfo(
                        eblanApplicationInfo = eblanApplicationInfo.copy(
                            customIcon = null,
                            customLabel = null,
                        ),
                    )
                }

                val newData = data.copy(
                    customIcon = null,
                    customLabel = null,
                )

                gridItem.copy(data = newData)
            }

            is GridItemData.ShortcutConfig -> {
                data.customIcon?.let { customIcon ->
                    val customIconFile = File(customIcon)

                    if (customIconFile.exists()) {
                        File(customIcon).delete()
                    }
                }

                val newData = data.copy(
                    customIcon = null,
                    customLabel = null,
                )

                gridItem.copy(data = newData)
            }

            is GridItemData.ShortcutInfo -> {
                data.customIcon?.let { customIcon ->
                    val customIconFile = File(customIcon)

                    if (customIconFile.exists()) {
                        File(customIcon).delete()
                    }
                }

                val newData = data.copy(
                    customIcon = null,
                    customShortLabel = null,
                )

                gridItem.copy(data = newData)
            }

            else -> gridItem
        }
    }

    override suspend fun updateGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val widgetGridItems = mutableListOf<WidgetGridItem>()

        val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val folderGridItems = mutableListOf<FolderGridItem>()

        val shortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItems.add(
                        gridItem.asApplicationInfo(data = data),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItems.add(
                        gridItem.asFolderGridItem(data = data),
                    )
                }

                is GridItemData.Widget -> {
                    widgetGridItems.add(
                        gridItem.asWidgetGridItem(data = data),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItems.add(
                        gridItem.asShortcutInfoGridItem(data = data),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItems.add(
                        gridItem.asShortcutConfigGridItem(data = data),
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

        shortcutConfigGridItemRepository.upsertShortcutConfigGridItems(
            shortcutConfigGridItems = shortcutConfigGridItems,
        )
    }

    override suspend fun deleteGridItems(gridItems: List<GridItem>) {
        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val widgetGridItems = mutableListOf<WidgetGridItem>()

        val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val folderGridItems = mutableListOf<FolderGridItem>()

        val shortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

        gridItems.forEach { gridItem ->
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    applicationInfoGridItems.add(
                        gridItem.asApplicationInfo(data = data),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItems.add(
                        gridItem.asFolderGridItem(data = data),
                    )
                }

                is GridItemData.Widget -> {
                    appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)

                    widgetGridItems.add(
                        gridItem.asWidgetGridItem(data = data),
                    )
                }

                is GridItemData.ShortcutInfo -> {
                    shortcutInfoGridItems.add(
                        gridItem.asShortcutInfoGridItem(data = data),
                    )
                }

                is GridItemData.ShortcutConfig -> {
                    shortcutConfigGridItems.add(
                        gridItem.asShortcutConfigGridItem(data = data),
                    )
                }
            }
        }

        applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
            applicationInfoGridItems = applicationInfoGridItems,
        )

        widgetGridItemRepository.deleteWidgetGridItemsByPackageName(widgetGridItems = widgetGridItems)

        shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = shortcutInfoGridItems)

        folderGridItemRepository.deleteFolderGridItems(folderGridItems = folderGridItems)

        shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(
            shortcutConfigGridItems = shortcutConfigGridItems,
        )
    }

    override suspend fun deleteGridItem(gridItem: GridItem) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                applicationInfoGridItemRepository.deleteApplicationInfoGridItem(
                    applicationInfoGridItem = ApplicationInfoGridItem(
                        id = gridItem.id,
                        folderId = gridItem.folderId,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
                        associate = gridItem.associate,
                        componentName = data.componentName,
                        packageName = data.packageName,
                        icon = data.icon,
                        label = data.label,
                        override = gridItem.override,
                        serialNumber = data.serialNumber,
                        customIcon = data.customIcon,
                        customLabel = data.customLabel,
                        gridItemSettings = gridItem.gridItemSettings,
                    ),
                )
            }

            is GridItemData.Folder -> {
                folderGridItemRepository.getFolderGridItemData(id = data.id)
                    ?.let { folderGridItemData ->
                        deleteGridItems(gridItems = folderGridItemData.gridItems)
                    }

                folderGridItemRepository.deleteFolderGridItem(
                    folderGridItem = FolderGridItem(
                        id = gridItem.id,
                        folderId = gridItem.folderId,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
                        associate = gridItem.associate,
                        label = data.label,
                        override = gridItem.override,
                        pageCount = data.pageCount,
                        icon = data.icon,
                        gridItemSettings = gridItem.gridItemSettings,
                    ),
                )
            }

            is GridItemData.ShortcutInfo -> {
                shortcutInfoGridItemRepository.deleteShortcutInfoGridItem(
                    shortcutInfoGridItem = ShortcutInfoGridItem(
                        id = gridItem.id,
                        folderId = gridItem.folderId,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
                        associate = gridItem.associate,
                        shortcutId = data.shortcutId,
                        packageName = data.packageName,
                        shortLabel = data.shortLabel,
                        longLabel = data.longLabel,
                        icon = data.icon,
                        override = gridItem.override,
                        serialNumber = data.serialNumber,
                        isEnabled = data.isEnabled,
                        customIcon = data.customIcon,
                        customShortLabel = data.customShortLabel,
                        eblanApplicationInfoIcon = data.eblanApplicationInfoIcon,
                        gridItemSettings = gridItem.gridItemSettings,
                    ),
                )
            }

            is GridItemData.Widget -> {
                appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)

                widgetGridItemRepository.deleteWidgetGridItem(
                    widgetGridItem = WidgetGridItem(
                        id = gridItem.id,
                        folderId = gridItem.folderId,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
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
                        label = data.label,
                        icon = data.icon,
                        override = gridItem.override,
                        serialNumber = data.serialNumber,
                        gridItemSettings = gridItem.gridItemSettings,
                    ),
                )
            }

            is GridItemData.ShortcutConfig -> {
                shortcutConfigGridItemRepository.deleteShortcutConfigGridItem(
                    shortcutConfigGridItem = ShortcutConfigGridItem(
                        id = gridItem.id,
                        folderId = gridItem.folderId,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
                        associate = gridItem.associate,
                        componentName = data.componentName,
                        packageName = data.packageName,
                        activityIcon = data.activityIcon,
                        activityLabel = data.activityLabel,
                        applicationIcon = data.applicationIcon,
                        applicationLabel = data.applicationLabel,
                        override = gridItem.override,
                        serialNumber = data.serialNumber,
                        shortcutIntentName = data.shortcutIntentName,
                        shortcutIntentIcon = data.shortcutIntentIcon,
                        shortcutIntentUri = data.shortcutIntentUri,
                        customIcon = data.customIcon,
                        customLabel = data.customLabel,
                        gridItemSettings = gridItem.gridItemSettings,
                    ),
                )
            }
        }
    }
}