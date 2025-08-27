package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.grid.getResolveDirectionByDiff
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ): List<GridItem>? {
        return withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = rows,
                    columns = columns,
                ) && when (resizingGridItem.associate) {
                    Associate.Grid -> {
                        gridItem.page == resizingGridItem.page &&
                                gridItem.associate == resizingGridItem.associate
                    }

                    Associate.Dock -> {
                        gridItem.associate == resizingGridItem.associate
                    }
                }
            }.toMutableList()

            val index =
                gridItems.indexOfFirst { gridItem -> gridItem.id == resizingGridItem.id }

            val oldGridItem = gridItems[index]

            gridItems[index] = resizingGridItem

            val resolveDirection = getResolveDirectionByDiff(
                old = oldGridItem,
                new = resizingGridItem,
            )

            val resolvedConflictsGridItems = resolveConflictsWhenMoving(
                gridItems = gridItems,
                resolveDirection = resolveDirection,
                moving = resizingGridItem,
                rows = rows,
                columns = columns,
            )

            if (resolvedConflictsGridItems != null) {
                gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
            }

            resolvedConflictsGridItems
        }
    }
}