package com.eblan.launcher.domain.usecase

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
        return withContext(Dispatchers.Default) {
            if (isGridItemSpanWithinBounds(
                    gridItem = movingGridItem,
                    rows = rows,
                    columns = columns,
                )
            ) {
                val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                    when (movingGridItem.associate) {
                        Associate.Grid -> {
                            isGridItemSpanWithinBounds(
                                gridItem = gridItem,
                                rows = rows,
                                columns = columns,
                            ) && gridItem.page == movingGridItem.page && gridItem.associate == Associate.Grid
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

                val index = gridItems.indexOfFirst { it.id == movingGridItem.id }

                if (index != -1) {
                    gridItems[index] = movingGridItem

                    val resolvedConflictsGridItems = resolveConflictsWhenMoving(
                        gridItems = gridItems,
                        movingGridItem = movingGridItem,
                        x = x,
                        y = y,
                        rows = rows,
                        columns = columns,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
                    )

                    if (resolvedConflictsGridItems != null) {
                        gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                    }

                    resolvedConflictsGridItems
                } else {
                    gridItems.add(movingGridItem)

                    val resolvedConflictsGridItems = resolveConflictsWhenMoving(
                        gridItems = gridItems,
                        movingGridItem = movingGridItem,
                        x = x,
                        y = y,
                        rows = rows,
                        columns = columns,
                        gridWidth = gridWidth,
                        gridHeight = gridHeight,
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
}