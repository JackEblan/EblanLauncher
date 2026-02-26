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
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(moveGridItemResult: MoveGridItemResult) {
        withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val folderColumns = homeSettings.folderColumns

            val folderRows = homeSettings.folderRows

            val gridItems = gridCacheRepository.gridItemsCache.first().toMutableList()

            val conflictingGridItem = moveGridItemResult.conflictingGridItem

            val movingIndex =
                gridItems.indexOfFirst { it.id == moveGridItemResult.movingGridItem.id }

            if (movingIndex != -1 && conflictingGridItem != null) {
                groupConflictingGridItemsIntoFolder(
                    gridItems = gridItems,
                    conflictingGridItem = conflictingGridItem,
                    movingGridItem = gridItems[movingIndex],
                    folderColumns = folderColumns,
                    folderRows = folderRows,
                    movingIndex = movingIndex,
                )
            }

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            gridRepository.updateGridItems(gridItems = gridItems)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun groupConflictingGridItemsIntoFolder(
        gridItems: MutableList<GridItem>,
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        folderColumns: Int,
        folderRows: Int,
        movingIndex: Int,
    ) {
        val conflictingIndex = gridItems.indexOfFirst { it.id == conflictingGridItem.id }

        when (val data = conflictingGridItem.data) {
            is GridItemData.Folder -> {
                addMovingGridItemIntoFolder(
                    conflictingData = data,
                    movingGridItem = movingGridItem,
                    folderColumns = folderColumns,
                    folderRows = folderRows,
                    gridItems = gridItems,
                    movingIndex = movingIndex,
                    conflictingGridItem = conflictingGridItem,
                    conflictingIndex = conflictingIndex,
                )
            }

            else -> {
                createNewFolder(
                    conflictingGridItem = conflictingGridItem,
                    movingGridItem = movingGridItem,
                    folderColumns = folderColumns,
                    folderRows = folderRows,
                    gridItems = gridItems,
                    conflictingIndex = conflictingIndex,
                    movingIndex = movingIndex,
                )
            }
        }
    }

    private suspend fun addMovingGridItemIntoFolder(
        conflictingData: GridItemData.Folder,
        movingGridItem: GridItem,
        folderColumns: Int,
        folderRows: Int,
        gridItems: MutableList<GridItem>,
        movingIndex: Int,
        conflictingGridItem: GridItem,
        conflictingIndex: Int,
    ) {
        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val newGridItem = findAvailableRegionByPage(
            gridItems = conflictingData.gridItems,
            gridItem = movingGridItem,
            pageCount = conflictingData.pageCount,
            columns = folderColumns,
            rows = folderRows,
        )

        if (newGridItem != null) {
            val newData = conflictingData.copy(gridItems = conflictingData.gridItems + newGridItem)

            gridItems[movingIndex] = newGridItem.copy(
                data = movingData.copy(folderId = conflictingData.id),
                associate = Associate.Grid,
            )

            gridItems[conflictingIndex] = conflictingGridItem.copy(data = newData)
        } else {
            val newPageCount = conflictingData.pageCount + 1

            val newData = conflictingData.copy(
                gridItems = conflictingData.gridItems + movingGridItem,
                pageCount = newPageCount,
            )

            gridItems[movingIndex] = movingGridItem.copy(
                page = newPageCount - 1,
                startColumn = 0,
                startRow = 0,
                data = movingData.copy(folderId = conflictingData.id),
                associate = Associate.Grid,
            )

            gridItems[conflictingIndex] = conflictingGridItem.copy(data = newData)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createNewFolder(
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        folderColumns: Int,
        folderRows: Int,
        gridItems: MutableList<GridItem>,
        conflictingIndex: Int,
        movingIndex: Int,
    ) {
        val movingData = movingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val conflictingData = conflictingGridItem.data as? GridItemData.ApplicationInfo
            ?: error("Expected GridItemData.ApplicationInfo")

        val id = Uuid.random().toHexString()

        val pageCount = 1

        val firstGridItem = conflictingGridItem.copy(
            page = 0,
            startColumn = 0,
            startRow = 0,
            data = conflictingData.copy(folderId = id),
            associate = Associate.Grid,
        )

        val secondGridItem = movingGridItem.copy(
            page = 0,
            startColumn = 0,
            startRow = 0,
            data = movingData.copy(folderId = id),
            associate = Associate.Grid,
        )

        val movedSecondGridItem = findAvailableRegionByPage(
            gridItems = listOf(firstGridItem),
            gridItem = secondGridItem,
            pageCount = pageCount,
            columns = folderColumns,
            rows = folderRows,
        )

        if (movedSecondGridItem != null) {
            val newData = GridItemData.Folder(
                id = id,
                label = "Unknown",
                gridItems = listOf(firstGridItem, movedSecondGridItem),
                pageCount = pageCount,
                icon = null,
            )

            gridItems[conflictingIndex] = firstGridItem

            gridItems[movingIndex] = movedSecondGridItem

            gridItems.add(
                conflictingGridItem.copy(
                    id = id,
                    data = newData,
                ),
            )
        } else {
            val newPageCount = pageCount + 1

            val newData = GridItemData.Folder(
                id = id,
                label = "Unknown",
                gridItems = emptyList(),
                pageCount = newPageCount,
                icon = null,
            )

            gridItems[conflictingIndex] = firstGridItem

            gridItems[movingIndex] = secondGridItem.copy(
                page = newPageCount - 1,
                startColumn = 0,
                startRow = 0,
                associate = Associate.Grid,
            )

            gridItems.add(
                conflictingGridItem.copy(
                    id = id,
                    data = newData,
                ),
            )
        }
    }
}
