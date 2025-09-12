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
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.grid.moveGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.ResolveDirection
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) {
        withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            val folderRows = homeSettings.folderRows

            val folderColumns = homeSettings.folderColumns

            val gridItems = gridCacheRepository.gridItemsCache.first().toMutableList()

            val movingIndex = gridItems.indexOfFirst { it.id == movingGridItem.id }

            if (movingIndex != -1 && conflictingGridItem != null) {
                groupConflictingGridItemsIntoFolder(
                    gridItems = gridItems,
                    conflictingGridItem = conflictingGridItem,
                    movingGridItem = movingGridItem,
                    folderRows = folderRows,
                    folderColumns = folderColumns,
                    movingIndex = movingIndex,
                )
            } else {
                updateGridItemsUseCase(gridItems = gridItems)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun groupConflictingGridItemsIntoFolder(
        gridItems: MutableList<GridItem>,
        conflictingGridItem: GridItem,
        movingGridItem: GridItem,
        folderRows: Int,
        folderColumns: Int,
        movingIndex: Int,
    ) {
        val conflictingIndex = gridItems.indexOfFirst { it.id == conflictingGridItem.id }

        when (val data = conflictingGridItem.data) {
            is GridItemData.Folder -> {
                val newGridItem = findAvailableRegionByPage(
                    gridItems = data.gridItems,
                    gridItem = movingGridItem,
                    rows = folderRows,
                    columns = folderColumns,
                    pageCount = data.pageCount,
                )

                if (newGridItem != null) {
                    gridItems[movingIndex] = newGridItem.copy(
                        folderId = conflictingGridItem.id,
                        associate = Associate.Grid,
                    )
                } else {
                    val newPageCount = data.pageCount + 1

                    val newData = data.copy(pageCount = newPageCount)

                    gridItems[movingIndex] = movingGridItem.copy(
                        page = newPageCount - 1,
                        startRow = 0,
                        startColumn = 0,
                        folderId = conflictingGridItem.id,
                        associate = Associate.Grid,
                    )

                    gridItems[conflictingIndex] = conflictingGridItem.copy(data = newData)
                }
            }

            else -> {
                val id = Uuid.random().toHexString()

                val firstGridItem = conflictingGridItem.copy(
                    page = 0,
                    startRow = 0,
                    startColumn = 0,
                    folderId = id,
                    associate = Associate.Grid,
                )

                val secondGridItem = movingGridItem.copy(
                    page = 0,
                    startRow = 0,
                    startColumn = 0,
                    folderId = id,
                    associate = Associate.Grid,
                )

                val movedSecondGridItem = moveGridItem(
                    resolveDirection = ResolveDirection.Right,
                    moving = firstGridItem,
                    conflicting = secondGridItem,
                    rows = folderRows,
                    columns = folderColumns,
                )

                if (movedSecondGridItem != null) {
                    val newData = GridItemData.Folder(
                        id = id,
                        label = "Unknown",
                        gridItems = emptyList(),
                        pageCount = 1,
                    )

                    gridItems[conflictingIndex] = firstGridItem

                    gridItems[movingIndex] = movedSecondGridItem

                    gridItems.add(
                        conflictingGridItem.copy(
                            id = id,
                            data = newData,
                        ),
                    )
                }
            }
        }

        updateGridItemsUseCase(gridItems = gridItems)
    }
}
