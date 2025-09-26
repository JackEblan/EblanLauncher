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
import com.eblan.launcher.domain.grid.getRelativeResolveDirection
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.grid.rectanglesOverlap
import com.eblan.launcher.domain.grid.resolveConflicts
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResizeGridItemUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        resizingGridItem: GridItem,
        columns: Int,
        rows: Int,
    ) {
        withContext(defaultDispatcher) {
            val gridItems = gridCacheRepository.gridItemsCache.first().filter { gridItem ->
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = columns,
                    rows = rows,
                ) && when (resizingGridItem.associate) {
                    Associate.Grid -> {
                        gridItem.page == resizingGridItem.page &&
                            gridItem.associate == resizingGridItem.associate
                    }

                    Associate.Dock -> {
                        gridItem.associate == resizingGridItem.associate
                    }
                }
            }.toMutableList()

            val index =
                gridItems.indexOfFirst { gridItem -> gridItem.id == resizingGridItem.id }

            val oldGridItem = gridItems[index]

            gridItems[index] = resizingGridItem

            val gridItemBySpan = gridItems.find { gridItem ->
                gridItem.id != resizingGridItem.id && rectanglesOverlap(
                    moving = resizingGridItem,
                    other = gridItem,
                )
            }

            if (gridItemBySpan != null) {
                val resolveDirection = getRelativeResolveDirection(
                    moving = oldGridItem,
                    other = gridItemBySpan,
                )

                val resolvedConflicts = resolveConflicts(
                    gridItems = gridItems,
                    resolveDirection = resolveDirection,
                    movingGridItem = resizingGridItem,
                    columns = columns,
                    rows = rows,
                )

                if (resolvedConflicts) {
                    gridCacheRepository.upsertGridItems(gridItems = gridItems)
                }
            } else {
                gridCacheRepository.upsertGridItems(gridItems = gridItems)
            }
        }
    }
}
