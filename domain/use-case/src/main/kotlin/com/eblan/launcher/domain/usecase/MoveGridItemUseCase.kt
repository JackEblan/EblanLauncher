package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.grid.getResolveDirectionBySpan
import com.eblan.launcher.domain.grid.getResolveDirectionByX
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.rectanglesOverlap
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        gridItems: List<GridItem>,
        movingGridItem: GridItem,
        x: Int,
        y: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): List<GridItem>? {
        return if (isGridItemSpanWithinBounds(
                gridItem = movingGridItem,
                rows = rows,
                columns = columns,
            )
        ) {
            withContext(Dispatchers.Default) {
                val gridItemsByAssociate = gridItems.filter { gridItem ->
                    gridItem.associate == movingGridItem.associate
                }.toMutableList()

                val index =
                    gridItemsByAssociate.indexOfFirst { gridItem -> gridItem.id == movingGridItem.id }

                if (index != -1) {
                    gridItemsByAssociate[index] = movingGridItem
                } else {
                    gridItemsByAssociate.add(movingGridItem)
                }

                val gridItemByCoordinates = getGridItemByCoordinates(
                    id = movingGridItem.id,
                    gridItems = gridItemsByAssociate,
                    rows = rows,
                    columns = columns,
                    x = x,
                    y = y,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                )

                val gridItemBySpan = gridItemsByAssociate.find { gridItem ->
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
                        gridItems = gridItemsByAssociate,
                        resolveDirection = resolveDirection,
                        moving = movingGridItem,
                        rows = rows,
                        columns = columns,
                    )
                } else if (gridItemBySpan != null) {
                    val resolveDirection = getResolveDirectionBySpan(
                        moving = movingGridItem,
                        other = gridItemBySpan,
                    )

                    resolveConflictsWhenMoving(
                        gridItems = gridItemsByAssociate,
                        resolveDirection = resolveDirection,
                        moving = movingGridItem,
                        rows = rows,
                        columns = columns,
                    )
                } else {
                    gridItemsByAssociate
                }

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