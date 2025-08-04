package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.grid.getResolveDirectionBySpan
import com.eblan.launcher.domain.grid.getResolveDirectionByX
import com.eblan.launcher.domain.grid.rectanglesOverlap
import com.eblan.launcher.domain.grid.resolveConflictsWhenFolding
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): List<GridItem>? {
        return withContext(Dispatchers.Default) {
            val index =
                gridItems.indexOfFirst { gridItem -> gridItem.id == movingGridItem.id }

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

            var resolvedConflictsGridItems: List<GridItem>? = null

            if (gridItemByCoordinates != null) {
                val resolveDirection = getResolveDirectionByX(
                    gridItem = gridItemByCoordinates,
                    x = x,
                    columns = columns,
                    gridWidth = gridWidth,
                )

                when (resolveDirection) {
                    ResolveDirection.Start, ResolveDirection.End -> {
                        resolvedConflictsGridItems = resolveConflictsWhenMoving(
                            gridItems = gridItems,
                            resolveDirection = resolveDirection,
                            moving = movingGridItem,
                            rows = rows,
                            columns = columns,
                        )
                    }

                    ResolveDirection.Center -> {
                        resolvedConflictsGridItems = resolveConflictsWhenFolding(
                            gridItems = gridItems,
                            moving = movingGridItem,
                        )
                    }
                }


            } else if (gridItemBySpan != null) {
                val resolveDirection = getResolveDirectionBySpan(
                    moving = movingGridItem,
                    other = gridItemBySpan,
                )

                resolvedConflictsGridItems = resolveConflictsWhenMoving(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    moving = movingGridItem,
                    rows = rows,
                    columns = columns,
                )
            } else {
                resolvedConflictsGridItems = gridItems
            }

            if (resolvedConflictsGridItems != null) {
                gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
            }

            resolvedConflictsGridItems
        }
    }
}