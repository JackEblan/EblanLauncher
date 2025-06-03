package com.eblan.launcher.domain.usecase

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
        return withContext(Dispatchers.Default) {
            if (isGridItemSpanWithinBounds(
                    gridItem = resizingGridItem,
                    rows = rows,
                    columns = columns,
                )
            ) {
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

                val index = gridItems.indexOfFirst { it.id == resizingGridItem.id }

                if (index != -1) {
                    val oldGridItem = gridItems[index]

                    gridItems[index] = resizingGridItem

                    val resolvedConflictsGridItems = resolveConflictsWhenResizing(
                        gridItems = gridItems,
                        oldGridItem = oldGridItem,
                        resizingGridItem = resizingGridItem,
                        rows = rows,
                        columns = columns,
                    )

                    if (resolvedConflictsGridItems != null) {
                        gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                    }

                    resolvedConflictsGridItems
                } else {
                    gridItems.add(resizingGridItem)

                    val resolvedConflictsGridItems = resolveConflictsWhenResizing(
                        gridItems = gridItems,
                        oldGridItem = resizingGridItem,
                        resizingGridItem = resizingGridItem,
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