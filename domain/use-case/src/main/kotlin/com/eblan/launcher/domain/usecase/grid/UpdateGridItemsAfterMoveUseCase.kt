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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.ApplicationInfoFolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.repository.ApplicationInfoFolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val gridRepository: GridRepository,
    private val applicationInfoFolderGridItemRepository: ApplicationInfoFolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(moveGridItemResult: MoveGridItemResult) {
        withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridItemsCache.first().toMutableList()

            val conflictingGridItem = moveGridItemResult.conflictingGridItem

            if (conflictingGridItem != null) {
                when (val data = conflictingGridItem.data) {
                    is GridItemData.Folder -> {
                        addMovingGridItemIntoFolder(
                            conflictingData = data,
                            movingGridItem = moveGridItemResult.movingGridItem,
                        )
                    }

                    else -> {
                        createNewFolder(
                            conflictingGridItem = conflictingGridItem,
                            movingGridItem = moveGridItemResult.movingGridItem,
                        )
                    }
                }
            }

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            gridRepository.updateGridItems(gridItems = gridItems)
        }
    }

    private suspend fun addMovingGridItemIntoFolder(
        conflictingData: GridItemData.Folder,
        movingGridItem: GridItem,
    ) {
        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        gridRepository.deleteGridItem(gridItem = movingGridItem)

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem =
                ApplicationInfoFolderGridItem(
                    id = movingGridItem.id,
                    folderId = conflictingData.id,
                    index = conflictingData.gridItemsByPage.size,
                    componentName = movingData.componentName,
                    packageName = movingData.packageName,
                    icon = movingData.icon,
                    label = movingData.label,
                    override = movingGridItem.override,
                    serialNumber = movingData.serialNumber,
                    customIcon = movingData.customIcon,
                    customLabel = movingData.customLabel,
                    gridItemSettings = movingGridItem.gridItemSettings,
                    doubleTap = movingGridItem.doubleTap,
                    swipeUp = movingGridItem.swipeUp,
                    swipeDown = movingGridItem.swipeDown,
                ),
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createNewFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
    ) {
        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val conflictingData = conflictingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val id = Uuid.random().toHexString()

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem =
                ApplicationInfoFolderGridItem(
                    id = conflictingGridItem.id,
                    folderId = id,
                    index = 0,
                    componentName = conflictingData.componentName,
                    packageName = conflictingData.packageName,
                    icon = conflictingData.icon,
                    label = conflictingData.label,
                    override = conflictingGridItem.override,
                    serialNumber = conflictingData.serialNumber,
                    customIcon = conflictingData.customIcon,
                    customLabel = conflictingData.customLabel,
                    gridItemSettings = conflictingGridItem.gridItemSettings,
                    doubleTap = conflictingGridItem.doubleTap,
                    swipeUp = conflictingGridItem.swipeUp,
                    swipeDown = conflictingGridItem.swipeDown,
                ),
        )

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem =
                ApplicationInfoFolderGridItem(
                    id = movingGridItem.id,
                    folderId = id,
                    index = 1,
                    componentName = movingData.componentName,
                    packageName = movingData.packageName,
                    icon = movingData.icon,
                    label = movingData.label,
                    override = movingGridItem.override,
                    serialNumber = movingData.serialNumber,
                    customIcon = movingData.customIcon,
                    customLabel = movingData.customLabel,
                    gridItemSettings = movingGridItem.gridItemSettings,
                    doubleTap = movingGridItem.doubleTap,
                    swipeUp = movingGridItem.swipeUp,
                    swipeDown = movingGridItem.swipeDown,
                ),
        )
    }
}
