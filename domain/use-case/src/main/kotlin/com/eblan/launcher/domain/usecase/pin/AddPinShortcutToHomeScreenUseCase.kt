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
package com.eblan.launcher.domain.usecase.pin

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemAction
import com.eblan.launcher.domain.model.GridItemActionType
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AddPinShortcutToHomeScreenUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        shortcutId: String,
        packageName: String,
        serialNumber: Long,
        shortLabel: String,
        longLabel: String,
        isEnabled: Boolean,
        icon: String?,
    ): GridItem? = withContext(defaultDispatcher) {
        val homeSettings = userDataRepository.userData.first().homeSettings

        val columns = homeSettings.columns

        val rows = homeSettings.rows

        val pageCount = homeSettings.pageCount

        val initialPage = homeSettings.initialPage

        val gridItems = (
            applicationInfoGridItemRepository.gridItems.first() +
                widgetGridItemRepository.gridItems.first() +
                shortcutInfoGridItemRepository.gridItems.first() +
                folderGridItemRepository.gridItems.first() +
                shortcutConfigGridItemRepository.gridItems.first()
            ).filter { gridItem ->
            gridItem.associate == Associate.Grid &&
                gridItem.folderId == null
        }

        val eblanApplicationInfoIcon =
            packageManagerWrapper.getComponentName(packageName = packageName)
                ?.let { componentName ->
                    val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                    val file = File(
                        directory,
                        componentName.hashCode().toString(),
                    )

                    file.absolutePath
                }

        val data = GridItemData.ShortcutInfo(
            shortcutId = shortcutId,
            packageName = packageName,
            serialNumber = serialNumber,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            isEnabled = isEnabled,
            eblanApplicationInfoIcon = eblanApplicationInfoIcon,
            customIcon = null,
            customShortLabel = null,
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
            gridItemAction = GridItemAction(
                gridItemActionType = GridItemActionType.None,
                componentName = "",
            ),
        )

        val newGridItem = findAvailableRegionByPage(
            gridItems = gridItems,
            gridItem = gridItem,
            pageCount = pageCount,
            columns = columns,
            rows = rows,
        )

        if (newGridItem != null) {
            gridCacheRepository.insertGridItems(gridItems = gridItems + newGridItem)
        }

        newGridItem
    }
}
