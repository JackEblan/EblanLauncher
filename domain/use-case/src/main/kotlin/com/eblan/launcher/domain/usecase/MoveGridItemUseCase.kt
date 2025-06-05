package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.ResolveDirection
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

            val firstConflictingGridItem = getFirstConflictingGridItem(
                id = movingGridItem.id,
                gridItems = gridItems,
                rows = rows,
                columns = columns,
                x = x,
                y = y,
                gridWidth = gridWidth,
                gridHeight = gridHeight,
            )

            val resolvedConflictsGridItems = if (firstConflictingGridItem != null) {
                val resolveDirection = getResolveDirection(
                    gridItem = firstConflictingGridItem,
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
            } else {
                gridItems
            }

            if (resolvedConflictsGridItems != null) {
                gridCacheRepository.upsertGridItems(resolvedConflictsGridItems)
            }

            resolvedConflictsGridItems
        }
    }

    private fun getFirstConflictingGridItem(
        id: String,
        gridItems: List<GridItem>,
        rows: Int,
        columns: Int,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): GridItem? {
        val cellWidth = gridWidth / columns

        val cellHeight = gridHeight / rows

        return gridItems.find { gridItem ->
            val startColumn = x / cellWidth

            val startRow = y / cellHeight

            val rowInSpan =
                startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

            val columnInSpan =
                startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

            gridItem.id != id && rowInSpan && columnInSpan
        }
    }
}

private fun getResolveDirection(
    gridItem: GridItem,
    x: Int,
    columns: Int,
    gridWidth: Int,
): ResolveDirection {
    val cellWidth = gridWidth / columns

    val gridItemX = gridItem.startColumn * cellWidth

    val gridItemWidth = gridItem.columnSpan * cellWidth

    val xInGridItem = x - gridItemX

    return when {
        xInGridItem < gridItemWidth / 3 -> {
            ResolveDirection.End
        }

        xInGridItem < 2 * gridItemWidth / 3 -> {
            ResolveDirection.Center
        }

        else -> {
            ResolveDirection.Start
        }
    }
}