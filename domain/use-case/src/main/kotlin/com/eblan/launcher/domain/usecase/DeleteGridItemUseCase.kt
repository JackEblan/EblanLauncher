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
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ShortcutConfigActivityGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigActivityGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteGridItemUseCase @Inject constructor(
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val deleteGridItemsUseCase: DeleteGridItemsUseCase,
    private val shortcutConfigActivityGridItemRepository: ShortcutConfigActivityGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItem: GridItem) {
        withContext(defaultDispatcher) {
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
                            gridItemSettings = gridItem.gridItemSettings,
                        ),
                    )
                }

                is GridItemData.Folder -> {
                    folderGridItemRepository.getFolderGridItemData(id = data.id)
                        ?.let { folderGridItemData ->
                            deleteGridItemsUseCase(gridItems = folderGridItemData.gridItems)
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
                            disabledMessage = data.disabledMessage,
                            gridItemSettings = gridItem.gridItemSettings,
                            eblanApplicationInfoIcon = data.eblanApplicationInfoIcon,
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

                is GridItemData.ShortcutConfigActivity -> {
                    shortcutConfigActivityGridItemRepository.deleteShortcutConfigActivityGridItem(
                        shortcutConfigActivityGridItem = ShortcutConfigActivityGridItem(
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
                            uri = data.uri,
                            uriIcon = data.uriIcon,
                            gridItemSettings = gridItem.gridItemSettings,
                        ),
                    )
                }
            }
        }
    }
}
