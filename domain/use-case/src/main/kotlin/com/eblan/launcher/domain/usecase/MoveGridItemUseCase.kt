package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.grid.getResolveDirectionBySpan
import com.eblan.launcher.domain.grid.getResolveDirectionByX
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.rectanglesOverlap
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

            if (index != -1) {
                gridItems[index] = movingGridItem
            } else {
                gridItems.add(movingGridItem)
            }

            val gridItemByCoordinates = getGridItemByCoordinates(
                id = movingGridItem.id,
                gridItems = gridItems,
                rows = rows,
                columns = columns,
                x = x,
                y = y,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            val gridItemBySpan = gridItems.find { gridItem ->
                gridItem.id != movingGridItem.id && rectanglesOverlap(
                    moving = movingGridItem,
                    other = gridItem,
                )
            }

            val resolvedConflictsGridItems = if (gridItemByCoordinates != null) {
                val resolveDirection = getResolveDirectionByX(
                    gridItem = gridItemByCoordinates,
                    x = x,
                    columns = columns,
                    gridWidth = gridWidth,
                )

                resolveConflictsWhenMoving(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movingGridItem,
                    rows = rows,
                    columns = columns,
                )
            } else if (gridItemBySpan != null) {
                val resolveDirection = getResolveDirectionBySpan(
                    moving = movingGridItem,
                    other = gridItemBySpan,
                )

                resolveConflictsWhenMoving(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = movingGridItem,
                    rows = rows,
                    columns = columns,
                )
            } else {
                gridItems
            }

            if (resolvedConflictsGridItems != null) {
                gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
            }

            resolvedConflictsGridItems
        }
    }
}