package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemsAfterMoveUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
) {
    suspend operator fun invoke(
        movingGridItem: GridItem,
        conflictingGridItem: GridItem?,
    ) {
        withContext(Dispatchers.Default) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                gridItem.page == movingGridItem.page
            }.toMutableList()

            if (conflictingGridItem != null) {
                val movingIndex =
                    gridItems.indexOfFirst { it.id == movingGridItem.id }

                val conflictingIndex =
                    gridItems.indexOfFirst { it.id == conflictingGridItem.id }

                when (conflictingGridItem.data) {
                    is GridItemData.Folder -> {
                        gridItems[movingIndex] =
                            movingGridItem.copy(folderId = conflictingGridItem.id)
                    }

                    else -> {
                        val folderId = conflictingGridItem.id

                        val conflictingGridItemWithNewFolderId =
                            conflictingGridItem.copy(folderId = folderId)

                        val movingGridItemWithNewFolderId = movingGridItem.copy(folderId = folderId)

                        val newData = GridItemData.Folder(
                            label = "Unknown",
                            gridItems = listOf(
                                movingGridItemWithNewFolderId,
                                conflictingGridItemWithNewFolderId,
                            ),
                        )

                        gridItems[conflictingIndex] = conflictingGridItemWithNewFolderId

                        gridItems[movingIndex] = movingGridItemWithNewFolderId

                        gridItems.add(conflictingGridItemWithNewFolderId.copy(data = newData))
                    }
                }
            }

            updateGridItemsUseCase(gridItems = gridItems)
        }
    }
}