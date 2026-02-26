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
import com.eblan.launcher.domain.model.FolderGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.repository.ApplicationInfoFolderGridItemRepository
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val gridRepository: GridRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val applicationInfoFolderGridItemRepository: ApplicationInfoFolderGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(moveGridItemResult: MoveGridItemResult) {
        withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridItemsCache.first().toMutableList()

            val conflictingGridItem = moveGridItemResult.conflictingGridItem

            if (conflictingGridItem != null) {
                when (conflictingGridItem.data) {
                    is GridItemData.Folder -> {
                        addMovingGridItemIntoFolder(
                            conflictingGridItem = conflictingGridItem,
                            movingGridItem = moveGridItemResult.movingGridItem,
                            gridItems = gridItems,
                        )
                    }

                    else -> {
                        createNewFolder(
                            conflictingGridItem = conflictingGridItem,
                            movingGridItem = moveGridItemResult.movingGridItem,
                            gridItems = gridItems,
                        )
                    }
                }
            }

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            gridRepository.updateGridItems(gridItems = gridItems)
        }
    }

    private suspend fun addMovingGridItemIntoFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        gridItems: MutableList<GridItem>,
    ) {
        val conflictingData = conflictingGridItem.data as? GridItemData.Folder
            ?: error("Expected GridItemData.Folder")

        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val applicationInfoFolderGridItem = ApplicationInfoFolderGridItem(
            id = movingGridItem.id,
            folderId = conflictingData.id,
            index = conflictingData.gridItems.size,
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
        )

        applicationInfoGridItemRepository.deleteApplicationInfoGridItemById(id = movingGridItem.id)

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem = applicationInfoFolderGridItem,
        )

        val conflictingIndex = gridItems.indexOf(conflictingGridItem)

        if (conflictingIndex != -1) {
            val conflictingDataGridItems = buildList(conflictingData.gridItems.size) {
                conflictingData.gridItems.forEach { applicationInfoFolderGridItem ->
                    if (applicationInfoFolderGridItem.id == movingGridItem.id) {
                        add(applicationInfoFolderGridItem)
                    } else {
                        add(applicationInfoFolderGridItem)
                    }
                }
            }

            val conflictingDataGridItemsByPage =
                conflictingData.gridItemsByPage.mapValues { (_, applicationInfoFolderGridItems) ->
                    buildList(applicationInfoFolderGridItems.size) {
                        applicationInfoFolderGridItems.forEach { applicationInfoFolderGridItem ->
                            if (applicationInfoFolderGridItem.id == movingGridItem.id) {
                                add(applicationInfoFolderGridItem)
                            } else {
                                add(applicationInfoFolderGridItem)
                            }
                        }
                    }
                }

            val newData = conflictingData.copy(
                gridItems = conflictingDataGridItems,
                gridItemsByPage = conflictingDataGridItemsByPage,
            )

            gridItems[conflictingIndex] = conflictingGridItem.copy(data = newData)
        }

        gridItems.remove(movingGridItem)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createNewFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        gridItems: MutableList<GridItem>,
    ) {
        val conflictingData = conflictingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val folderId = conflictingGridItem.id

        val conflictingApplicationInfoFolderGridItem = ApplicationInfoFolderGridItem(
            id = conflictingGridItem.id,
            folderId = folderId,
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
        )

        val movingApplicationInfoFolderGridItem = ApplicationInfoFolderGridItem(
            id = movingGridItem.id,
            folderId = folderId,
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
        )

        applicationInfoGridItemRepository.deleteApplicationInfoGridItemById(id = conflictingGridItem.id)

        applicationInfoGridItemRepository.deleteApplicationInfoGridItemById(id = movingGridItem.id)

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem = conflictingApplicationInfoFolderGridItem,
        )

        applicationInfoFolderGridItemRepository.insertApplicationInfoFolderGridItem(
            applicationInfoFolderGridItem = movingApplicationInfoFolderGridItem,
        )

        val folderGridItem = FolderGridItem(
            id = folderId,
            page = conflictingGridItem.page,
            startColumn = conflictingGridItem.startColumn,
            startRow = conflictingGridItem.startRow,
            columnSpan = conflictingGridItem.columnSpan,
            rowSpan = conflictingGridItem.rowSpan,
            associate = conflictingGridItem.associate,
            label = "Unknown",
            override = conflictingGridItem.override,
            icon = null,
            gridItemSettings = conflictingGridItem.gridItemSettings,
            doubleTap = conflictingGridItem.doubleTap,
            swipeUp = conflictingGridItem.swipeUp,
            swipeDown = conflictingGridItem.swipeDown,
        )

        folderGridItemRepository.insertFolderGridItem(folderGridItem = folderGridItem)

        gridItems.remove(conflictingGridItem)

        gridItems.remove(movingGridItem)

        val applicationInfoFolderGridItems = listOf(
            conflictingApplicationInfoFolderGridItem,
            movingApplicationInfoFolderGridItem,
        )

        val newData = GridItemData.Folder(
            id = folderId,
            label = "Unknown",
            gridItems = applicationInfoFolderGridItems,
            gridItemsByPage = mapOf(0 to applicationInfoFolderGridItems),
            icon = null,
            columns = 1,
            rows = 2,
        )

        gridItems.add(
            GridItem(
                id = folderId,
                page = conflictingGridItem.page,
                startColumn = conflictingGridItem.startColumn,
                startRow = conflictingGridItem.startRow,
                columnSpan = conflictingGridItem.columnSpan,
                rowSpan = conflictingGridItem.rowSpan,
                data = newData,
                associate = conflictingGridItem.associate,
                override = conflictingGridItem.override,
                gridItemSettings = conflictingGridItem.gridItemSettings,
                doubleTap = conflictingGridItem.doubleTap,
                swipeUp = conflictingGridItem.swipeUp,
                swipeDown = conflictingGridItem.swipeDown,
            ),
        )
    }
}
