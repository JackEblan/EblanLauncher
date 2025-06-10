package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getResolveDirectionByDiff
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenResizing
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ): List<GridItem>? {
        if (!isGridItemSpanWithinBounds(
                gridItem = resizingGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            return null
        }

        return withContext(Dispatchers.Default) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                when (resizingGridItem.associate) {
                    Associate.Grid -> {
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            rows = rows,
                            columns = columns,
                        ) && gridItem.page == resizingGridItem.page && gridItem.associate == Associate.Grid
                    }

                    Associate.Dock -> {
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            rows = rows,
                            columns = columns,
                        ) && gridItem.associate == Associate.Dock
                    }
                }
            }.toMutableList()

            val index = gridItems.indexOfFirst { gridItem -> gridItem.id == resizingGridItem.id }

            val oldGridItem = gridItems[index]

            gridItems[index] = resizingGridItem

            val resolveDirection = getResolveDirectionByDiff(
                oldGridItem = oldGridItem,
                newGridItem = resizingGridItem,
            )

            val resolvedConflictsGridItems = resolveConflictsWhenResizing(
                gridItems = gridItems,
                resolveDirection = resolveDirection,
                resizingGridItem = resizingGridItem,
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