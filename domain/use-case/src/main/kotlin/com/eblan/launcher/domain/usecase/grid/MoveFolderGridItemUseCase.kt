package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveFolderGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @param:Dispatcher(EblanDispatchers.Default)
    private val defaultDispatcher: CoroutineDispatcher,
) {

    suspend operator fun invoke(
        applicationInfoGridItems: List<ApplicationInfoGridItem>,
        movingApplicationInfoGridItem: ApplicationInfoGridItem,
        dragX: Int,
        dragY: Int,
        columns: Int,
        rows: Int,
        gridWidth: Int,
        gridHeight: Int,
        lockMovement: Boolean,
    ) {
        return withContext(defaultDispatcher) {

            val gridItems = gridCacheRepository.gridItemsCache
                .first()
                .filter { gridItem ->
                    gridItem.page == movingApplicationInfoGridItem.page &&
                            gridItem.associate == movingApplicationInfoGridItem.associate &&
                            gridItem.data is GridItemData.ApplicationInfo
                }
                .toMutableList()

            val fromIndex =
                gridItems.indexOfFirst { it.id == movingApplicationInfoGridItem.id }

            if (fromIndex == -1) {
                return@withContext
            }

            val cellWidth = gridWidth / columns
            val cellHeight = gridHeight / rows

            var targetColumn = dragX / cellWidth
            var targetRow = dragY / cellHeight

            targetColumn = targetColumn.coerceIn(0, columns - 1)
            targetRow = targetRow.coerceIn(0, rows - 1)

            var toIndex = targetRow * columns + targetColumn
            toIndex = toIndex.coerceIn(0, gridItems.lastIndex)

            if (toIndex == fromIndex) {
                return@withContext
            }

            val item = gridItems.removeAt(fromIndex)

            if (toIndex > fromIndex) {
                gridItems.add(toIndex, item)
            } else {
                gridItems.add(toIndex, item)
            }

            if (!lockMovement) {
                gridCacheRepository.upsertGridItems(
                    gridItems = gridItems,
                )
            }
        }
    }
}