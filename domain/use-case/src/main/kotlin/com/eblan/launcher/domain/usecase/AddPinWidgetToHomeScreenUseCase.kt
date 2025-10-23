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
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.grid.getWidgetGridItemSize
import com.eblan.launcher.domain.grid.getWidgetGridItemSpan
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AddPinWidgetToHomeScreenUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        className: String,
        componentName: String,
        configure: String?,
        packageName: String,
        serialNumber: Long,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
    ): GridItem? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val columns = homeSettings.columns

            val rows = homeSettings.rows

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val dockHeight = homeSettings.dockHeight

            val gridItems = applicationInfoGridItemRepository.gridItems.first() +
                    widgetGridItemRepository.gridItems.first() +
                    shortcutInfoGridItemRepository.gridItems.first() +
                    folderGridItemRepository.gridItems.first()

            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = packageName)
                    ?: return@withContext null

            val previewInferred = File(
                fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                className,
            ).absolutePath

            val gridHeight = rootHeight - dockHeight

            val cellWidth = rootWidth / columns

            val cellHeight = gridHeight / rows

            val (checkedColumnSpan, checkedRowSpan) = getWidgetGridItemSpan(
                cellHeight = cellHeight,
                cellWidth = cellWidth,
                minHeight = minHeight,
                minWidth = minWidth,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
            )

            val (checkedMinWidth, checkedMinHeight) = getWidgetGridItemSize(
                columns = columns,
                rows = rows,
                gridWidth = rootWidth,
                gridHeight = gridHeight,
                minWidth = minWidth,
                minHeight = minHeight,
                targetCellWidth = targetCellWidth,
                targetCellHeight = targetCellHeight,
            )

            val data = GridItemData.Widget(
                appWidgetId = 0,
                className = className,
                componentName = componentName,
                packageName = packageName,
                serialNumber = serialNumber,
                configure = configure,
                minWidth = checkedMinWidth,
                minHeight = checkedMinHeight,
                resizeMode = resizeMode,
                minResizeWidth = minResizeWidth,
                minResizeHeight = minResizeHeight,
                maxResizeWidth = maxResizeWidth,
                maxResizeHeight = maxResizeHeight,
                targetCellHeight = targetCellHeight,
                targetCellWidth = targetCellWidth,
                preview = previewInferred,
                eblanApplicationInfo = eblanApplicationInfo,
            )

            val gridItem = GridItem(
                id = Uuid.random().toHexString(),
                folderId = null,
                page = initialPage,
                startColumn = 0,
                startRow = 0,
                columnSpan = checkedColumnSpan,
                rowSpan = checkedRowSpan,
                data = data,
                associate = Associate.Grid,
                override = false,
                gridItemSettings = homeSettings.gridItemSettings,
            )

            val newGridItem = findAvailableRegionByPage(
                gridItems = gridItems,
                gridItem = gridItem,
                pageCount = pageCount,
                columns = columns,
                rows = rows,
            )

            if (newGridItem != null) {
                gridCacheRepository.insertGridItems(gridItems = gridItems)

                gridCacheRepository.insertGridItem(gridItem = newGridItem)
            }

            newGridItem
        }
    }
}
