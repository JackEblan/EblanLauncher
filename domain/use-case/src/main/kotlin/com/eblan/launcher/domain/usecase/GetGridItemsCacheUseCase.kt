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
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemCacheType
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetGridItemsCacheUseCase @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<GridItemCache> {
        return combine(
            userDataRepository.userData,
            gridCacheRepository.gridItemsCache,
            gridCacheRepository.gridItemCacheType,
        ) { userData, gridItems, gridItemCacheType ->
            when (gridItemCacheType) {
                GridItemCacheType.Grid -> {
                    val gridItemsCacheByPage = gridItems.filter { gridItem ->
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            columns = userData.homeSettings.columns,
                            rows = userData.homeSettings.rows,
                        ) && gridItem.associate == Associate.Grid
                    }.groupBy { gridItem -> gridItem.page }

                    val dockGridItemsCache = gridItems.filter { gridItem ->
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            columns = userData.homeSettings.dockColumns,
                            rows = userData.homeSettings.dockRows,
                        ) && gridItem.associate == Associate.Dock
                    }

                    GridItemCache(
                        gridItemCacheType = gridItemCacheType,
                        gridItemsCacheByPage = gridItemsCacheByPage,
                        dockGridItemsCache = dockGridItemsCache,
                    )
                }

                GridItemCacheType.Folder -> {
                    val gridItemsCacheByPage = gridItems.filter { gridItem ->
                        isGridItemSpanWithinBounds(
                            gridItem = gridItem,
                            columns = userData.homeSettings.folderColumns,
                            rows = userData.homeSettings.folderRows,
                        ) && gridItem.associate == Associate.Grid
                    }.groupBy { gridItem -> gridItem.page }

                    GridItemCache(
                        gridItemCacheType = gridItemCacheType,
                        gridItemsCacheByPage = gridItemsCacheByPage,
                        dockGridItemsCache = emptyList(),
                    )
                }
            }
        }.flowOn(defaultDispatcher)
    }
}
