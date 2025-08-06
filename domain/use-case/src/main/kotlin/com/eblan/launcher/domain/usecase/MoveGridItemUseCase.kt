package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.grid.getGridItemByCoordinates
import com.eblan.launcher.domain.grid.getResolveDirectionBySpan
import com.eblan.launcher.domain.grid.getResolveDirectionByX
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.rectanglesOverlap
import com.eblan.launcher.domain.grid.resolveConflictsWhenMoving
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.MoveGridItemResult
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
    ): MoveGridItemResult {
        return withContext(Dispatchers.Default) {
            val gridItems = gridCacheRepository.gridCacheItems.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = rows,
                    columns = columns,
                ) && gridItem.page == movingGridItem.page &&
                        gridItem.associate == movingGridItem.associate
            }.toMutableList()

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

            if (gridItemByCoordinates != null) {
                return@withContext handleConflictsOfGridItemCoordinates(
                    gridItemByCoordinates = gridItemByCoordinates,
                    x = x,
                    columns = columns,
                    gridWidth = gridWidth,
                    gridItems = gridItems,
                    movingGridItem = movingGridItem,
                    rows = rows,
                )
            }

            if (gridItemBySpan != null) {
                return@withContext handleConflictsOfGridItemSpan(
                    movingGridItem,
                    gridItemBySpan,
                    gridItems,
                    rows,
                    columns,
                )
            }

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            return@withContext MoveGridItemResult(
                gridItems = gridItems,
                movingGridItem = movingGridItem,
                conflictingGridItem = null,
            )
        }
    }

    private suspend fun handleConflictsOfGridItemSpan(
        movingGridItem: GridItem,
        gridItemBySpan: GridItem,
        gridItems: MutableList<GridItem>,
        rows: Int,
        columns: Int,
    ): MoveGridItemResult {
        val resolvedConflictsGridItems: List<GridItem>?

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

        if (resolvedConflictsGridItems != null) {
            gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
        }

        return MoveGridItemResult(
            gridItems = resolvedConflictsGridItems,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )
    }

    private suspend fun handleConflictsOfGridItemCoordinates(
        gridItemByCoordinates: GridItem,
        x: Int,
        columns: Int,
        gridWidth: Int,
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        rows: Int,
    ): MoveGridItemResult {
        val resolvedConflictsGridItems: List<GridItem>?

        val conflictingGridItem: GridItem?

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

                conflictingGridItem = null
            }

            ResolveDirection.Center -> {
                resolvedConflictsGridItems = gridItems

                conflictingGridItem = gridItemByCoordinates
//                // Only Application is allowed to enter folder like other launchers do
//                val movingGridItemDataAllowed = movingGridItem.data is GridItemData.ApplicationInfo
//
//                // Only Folder and application is allowed to group with other grid items
//                val conflictingGridItemDataAllowed =
//                    gridItemByCoordinates.data is GridItemData.Folder ||
//                            gridItemByCoordinates.data is GridItemData.ApplicationInfo
//
//                if (movingGridItemDataAllowed && conflictingGridItemDataAllowed) {
//                    resolvedConflictsGridItems = gridItems
//
//                    conflictingGridItem = gridItemByCoordinates
//                } else {
//                    resolvedConflictsGridItems = null
//
//                    conflictingGridItem = null
//                }
            }
        }

        if (resolvedConflictsGridItems != null) {
            gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
        }

        return MoveGridItemResult(
            gridItems = resolvedConflictsGridItems,
            movingGridItem = movingGridItem,
            conflictingGridItem = conflictingGridItem,
        )
    }
}