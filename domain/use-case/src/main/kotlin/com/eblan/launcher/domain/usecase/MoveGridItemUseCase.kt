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

                val gridItemByCoordinates = getGridItemByCoordinates(
                    movingGridItem = movingGridItem,
                    gridItems = gridItems,
                    rows = rows,
                    columns = columns,
                    x = x,
                    y = y,
                    gridWidth = gridWidth,
                    gridHeight = gridHeight,
                )

                val index = gridItems.indexOfFirst { it.id == movingGridItem.id }

                if (index != -1) {
                    gridItems[index] = movingGridItem

                    if (gridItemByCoordinates != null) {
                        val resolveDirection = getResolveDirection(
                            gridItem = gridItemByCoordinates,
                            x = x,
                            columns = columns,
                            gridWidth = gridWidth,
                        )

                        val resolvedConflictsGridItems = resolveConflictsWhenMoving(
                            gridItems = gridItems,
                            resolveDirection = resolveDirection,
                            movingGridItem = movingGridItem,
                            x = x,
                            rows = rows,
                            columns = columns,
                            gridWidth = gridWidth,
                        )

                        println(resolvedConflictsGridItems)

                        if (resolvedConflictsGridItems != null) {
                            gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                        }

                        resolvedConflictsGridItems
                    } else {
                        gridCacheRepository.upsertGridItems(gridItems = gridItems)

                        gridItems
                    }
                } else {
                    gridItems.add(movingGridItem)

                    if (gridItemByCoordinates != null) {
                        val resolveDirection = getResolveDirection(
                            gridItem = gridItemByCoordinates,
                            x = x,
                            columns = columns,
                            gridWidth = gridWidth,
                        )

                        val resolvedConflictsGridItems = resolveConflictsWhenMoving(
                            gridItems = gridItems,
                            resolveDirection = resolveDirection,
                            movingGridItem = movingGridItem,
                            x = x,
                            rows = rows,
                            columns = columns,
                            gridWidth = gridWidth,
                        )

                        if (resolvedConflictsGridItems != null) {
                            gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
                        }

                        resolvedConflictsGridItems
                    } else {
                        gridCacheRepository.upsertGridItems(gridItems = gridItems)

                        gridItems
                    }
                }
            } else {
                null
            }
        }
    }

    private fun getGridItemByCoordinates(
        movingGridItem: GridItem,
        gridItems: List<GridItem>,
        rows: Int,
        columns: Int,
        x: Int,
        y: Int,
        gridWidth: Int,
        gridHeight: Int,
    ): GridItem? {
        val cellWidth = gridWidth / rows

        val cellHeight = gridHeight / columns

        return gridItems.find { gridItem ->
            val startRow = y / cellHeight

            val startColumn = x / cellWidth

            val rowInSpan =
                startRow in gridItem.startRow until (gridItem.startRow + gridItem.rowSpan)

            val columnInSpan =
                startColumn in gridItem.startColumn until (gridItem.startColumn + gridItem.columnSpan)

            gridItem.page == movingGridItem.page && gridItem.id != movingGridItem.id && rowInSpan && columnInSpan
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