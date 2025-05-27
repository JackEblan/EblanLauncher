package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWithShift
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShiftAlgorithmUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
) {
    suspend operator fun invoke(
        movingGridItem: GridItem,
        rows: Int,
        columns: Int,
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

                if(index != -1) {
                    val oldGridItem = gridItems[index]

                    gridItems[index] = movingGridItem

                    val resolvedConflictsGridItems = resolveConflictsWithShift(
                        gridItems = gridItems,
                        oldGridItem = oldGridItem,
                        movingGridItem = movingGridItem,
                        rows = rows,
                        columns = columns,
                    )

                    if (resolvedConflictsGridItems != null) {
                        gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                    }

                    resolvedConflictsGridItems
                } else {
                    gridItems.add(movingGridItem)

                    val resolvedConflictsGridItems = resolveConflictsWithShift(
                        gridItems = gridItems,
                        oldGridItem = movingGridItem,
                        movingGridItem = movingGridItem,
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
}