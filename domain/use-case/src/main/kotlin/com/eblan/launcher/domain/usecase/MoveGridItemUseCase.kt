package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemWhenXConflicts
import com.eblan.launcher.domain.grid.getResolveDirectionWhenXConflicts
import com.eblan.launcher.domain.grid.getResolveDirectionWhenXNotConflicts
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): List<GridItem>? {
        if (!isGridItemSpanWithinBounds(
                gridItem = movingGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            return null
        }

        return withContext(Dispatchers.Default) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                when (movingGridItem.associate) {
                    Associate.Grid -> {
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            rows = rows,
                            columns = columns,
                        ) && gridItem.page == movingGridItem.page && gridItem.associate == movingGridItem.associate
                    }

                    Associate.Dock -> {
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            rows = rows,
                            columns = columns,
                        ) && gridItem.associate == movingGridItem.associate
                    }
                }
            }.toMutableList()

            val index = gridItems.indexOfFirst { gridItem -> gridItem.id == movingGridItem.id }

            val oldGridItem = if (index != -1) {
                gridItems[index]
            } else {
                null
            }

            if (index != -1) {
                gridItems[index] = movingGridItem
            } else {
                gridItems.add(movingGridItem)
            }

            val gridItemWhenXConflicts = getGridItemWhenXConflicts(
                id = movingGridItem.id,
                gridItems = gridItems,
                rows = rows,
                columns = columns,
                x = x,
                y = y,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            val resolvedConflictsGridItems = if (gridItemWhenXConflicts != null) {
                val resolveDirection = getResolveDirectionWhenXConflicts(
                    gridItem = gridItemWhenXConflicts,
                    x = x,
                    columns = columns,
                    gridWidth = gridWidth,
                )

                resolveConflictsWhenMoving(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movingGridItem,
                    x = x,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                )
            } else if (oldGridItem != null) {
                val resolveDirection = getResolveDirectionWhenXNotConflicts(
                    oldGridItem = oldGridItem,
                    newGridItem = movingGridItem,
                )

                resolveConflictsWhenMoving(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movingGridItem,
                    x = x,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                )
            } else {
                null
            }

            if (resolvedConflictsGridItems != null) {
                gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
            }

            resolvedConflictsGridItems
        }
    }
}