/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItems: List<GridItem>) {
        withContext(defaultDispatcher) {
            val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val widgetGridItems = mutableListOf<WidgetGridItem>()

            val shortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val folderGridItems = mutableListOf<FolderGridItem>()

            val shortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

            gridItems.forEach { gridItem ->
                when (val data = gridItem.data) {
                    is GridItemData.ApplicationInfo -> {
                        applicationInfoGridItems.add(
                            ApplicationInfoGridItem(
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
                        folderGridItems.add(
                            FolderGridItem(
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
                                gridItemSettings = gridItem.gridItemSettings,
                            ),
                        )
                    }

                    is GridItemData.Widget -> {
                        widgetGridItems.add(
                            WidgetGridItem(
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

                    is GridItemData.ShortcutInfo -> {
                        shortcutInfoGridItems.add(
                            ShortcutInfoGridItem(
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
                                disabledMessage = data.disabledMessage,
                                eblanApplicationInfoIcon = data.eblanApplicationInfoIcon,
                                customIcon = data.customIcon,
                                customLabel = data.customLabel,
                                gridItemSettings = gridItem.gridItemSettings,
                            ),
                        )
                    }

                    is GridItemData.ShortcutConfig -> {
                        shortcutConfigGridItems.add(
                            ShortcutConfigGridItem(
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
                                gridItemSettings = gridItem.gridItemSettings,
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

            shortcutConfigGridItemRepository.upsertShortcutConfigGridItems(
                shortcutConfigGridItems = shortcutConfigGridItems,
            )
        }
    }
}
