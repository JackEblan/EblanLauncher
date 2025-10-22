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
import javax.inject.Inject

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
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
    suspend operator fun invoke(
        shortcutId: String,
        packageName: String,
        serialNumber: Long,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray?,
    ): GridItem? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val columns = homeSettings.columns

            val rows = homeSettings.rows

            val pageCount = homeSettings.pageCount

            val initialPage = homeSettings.initialPage

            val gridItems = applicationInfoGridItemRepository.applicationInfoGridItems.first() +
                    widgetGridItemRepository.widgetGridItems.first() +
                    shortcutInfoGridItemRepository.shortcutInfoGridItems.first() +
                    folderGridItemRepository.folderGridItems.first()

            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = packageName)

            if (eblanApplicationInfo != null) {
                val icon = byteArray?.let { currentByteArray ->
                    fileManager.getAndUpdateFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                        name = shortcutId,
                        byteArray = currentByteArray,
                    )
                }

                val data = GridItemData.ShortcutInfo(
                    shortcutId = shortcutId,
                    packageName = packageName,
                    serialNumber = serialNumber,
                    shortLabel = shortLabel,
                    longLabel = longLabel,
                    icon = icon,
                    eblanApplicationInfo = eblanApplicationInfo,
                )

                val gridItem = GridItem(
                    id = shortcutId,
                    folderId = null,
                    page = initialPage,
                    startColumn = 0,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
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
                    gridCacheRepository.insertGridItem(gridItem = newGridItem)
                }

                newGridItem
            } else {
                null
            }
        }
    }
}
