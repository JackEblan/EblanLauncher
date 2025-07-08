package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getResolveDirectionByDiff
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        gridItems: MutableList<GridItem>,
        resizingGridItem: GridItem,
        rows: Int,
        columns: Int,
    ): List<GridItem>? {
        return if (isGridItemSpanWithinBounds(
                gridItem = resizingGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            withContext(Dispatchers.Default) {
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
        } else {
            null
        }
    }
}