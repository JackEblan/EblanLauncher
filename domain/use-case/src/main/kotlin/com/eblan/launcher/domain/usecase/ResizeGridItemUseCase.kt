package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenResizing
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
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

            val resolveDirection = getResolveDirection(
                oldGridItem = oldGridItem,
                resizingGridItem = resizingGridItem,
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

    private fun getResolveDirection(
        oldGridItem: GridItem,
        resizingGridItem: GridItem,
    ): ResolveDirection {
        val oldCenterRow = oldGridItem.startRow + oldGridItem.rowSpan / 2.0
        val oldCenterColumn = oldGridItem.startColumn + oldGridItem.columnSpan / 2.0

        val newCenterRow = resizingGridItem.startRow + resizingGridItem.rowSpan / 2.0
        val newCenterColumn = resizingGridItem.startColumn + resizingGridItem.columnSpan / 2.0

        val rowDiff = newCenterRow - oldCenterRow
        val columnDiff = newCenterColumn - oldCenterColumn

        return when {
            rowDiff < 0 || columnDiff < 0 -> ResolveDirection.Start
            rowDiff > 0 || columnDiff > 0 -> ResolveDirection.End
            else -> ResolveDirection.Center
        }
    }
}