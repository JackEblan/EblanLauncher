/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.domain.usecase

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveFolderGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
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
        return withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridItemsCache.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    rows = rows,
                    columns = columns,
                ) && gridItem.page == movingGridItem.page
            }.toMutableList()

            val index =
                gridItems.indexOfFirst { gridItem -> gridItem.id == movingGridItem.id }

            gridItems[index] = movingGridItem

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
                    gridItems = gridItems,
                    movingGridItem = movingGridItem,
                    gridItemByCoordinates = gridItemByCoordinates,
                    x = x,
                    rows = rows,
                    columns = columns,
                    gridWidth = gridWidth,
                )
            }

            if (gridItemBySpan != null) {
                return@withContext handleConflictsOfGridItemSpan(
                    movingGridItem = movingGridItem,
                    gridItemBySpan = gridItemBySpan,
                    gridItems = gridItems,
                    rows = rows,
                    columns = columns,
                )
            }

            gridCacheRepository.upsertGridItems(gridItems = gridItems)

            return@withContext MoveGridItemResult(
                isSuccess = true,
                movingGridItem = movingGridItem,
                conflictingGridItem = null,
            )
        }
    }

    private suspend fun handleConflictsOfGridItemCoordinates(
        gridItems: MutableList<GridItem>,
        movingGridItem: GridItem,
        gridItemByCoordinates: GridItem,
        x: Int,
        rows: Int,
        columns: Int,
        gridWidth: Int,
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
            ResolveDirection.Left, ResolveDirection.Right -> {
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
                resolvedConflictsGridItems = null

                conflictingGridItem = null
            }
        }

        if (resolvedConflictsGridItems != null) {
            gridCacheRepository.upsertGridItems(gridItems = resolvedConflictsGridItems)
        }

        return MoveGridItemResult(
            isSuccess = resolvedConflictsGridItems != null,
            movingGridItem = movingGridItem,
            conflictingGridItem = conflictingGridItem,
        )
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
            isSuccess = resolvedConflictsGridItems != null,
            movingGridItem = movingGridItem,
            conflictingGridItem = null,
        )
    }
}
