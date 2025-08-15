package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.grid.findAvailableRegion
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
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
            updateGridItems(
                movingGridItem = movingGridItem,
                conflictingGridItem = conflictingGridItem,
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun updateGridItems(
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) {
        val homeSettings = userDataRepository.userData.first().homeSettings

        val rows = if (movingGridItem.associate == Associate.Grid) {
            homeSettings.rows
        } else {
            homeSettings.dockRows
        }

        val columns = if (movingGridItem.associate == Associate.Grid) {
            homeSettings.columns
        } else {
            homeSettings.dockColumns
        }

        val folderRows = homeSettings.folderRows

        val folderColumns = homeSettings.folderColumns

        val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
            isGridItemSpanWithinBounds(
                gridItem = gridItem,
                rows = rows,
                columns = columns,
            ) && gridItem.page == movingGridItem.page &&
                    gridItem.associate == movingGridItem.associate

        }.toMutableList()

        val movingIndex =
            gridItems.indexOfFirst { it.id == movingGridItem.id }

        if (movingIndex != -1 && conflictingGridItem == null) {
            updateGridItemsUseCase(gridItems = gridItems)

            return
        }

        if (movingIndex != -1 && conflictingGridItem != null) {
            val conflictingIndex =
                gridItems.indexOfFirst { it.id == conflictingGridItem.id }

            when (val data = conflictingGridItem.data) {
                is GridItemData.Folder -> {
                    val newGridItem = findAvailableRegion(
                        page = conflictingGridItem.page,
                        gridItems = data.gridItems,
                        gridItem = movingGridItem,
                        rows = folderRows,
                        columns = folderColumns,
                    )

                    if (newGridItem != null) {
                        gridItems[movingIndex] =
                            newGridItem.copy(folderId = conflictingGridItem.id)

                        updateGridItemsUseCase(gridItems = gridItems)
                    }
                }

                else -> {
                    val id = Uuid.random().toHexString()

                    val firstGridItem =
                        conflictingGridItem.copy(
                            startRow = 0,
                            startColumn = 0,
                            folderId = id,
                        )

                    val secondGridItem =
                        movingGridItem.copy(
                            startRow = 0,
                            startColumn = 0,
                            folderId = id,
                        )

                    val movedSecondGridItem = moveGridItem(
                        resolveDirection = ResolveDirection.End,
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
                        )

                        gridItems[conflictingIndex] = firstGridItem

                        gridItems[movingIndex] = movedSecondGridItem

                        gridItems.add(
                            conflictingGridItem.copy(
                                id = id,
                                data = newData,
                            ),
                        )

                        updateGridItemsUseCase(gridItems = gridItems)
                    }
                }
            }
        }
    }
}