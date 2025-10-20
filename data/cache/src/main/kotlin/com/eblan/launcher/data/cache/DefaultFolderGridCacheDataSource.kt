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
package com.eblan.launcher.data.cache

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.GridItem
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultFolderGridCacheDataSource @Inject constructor(@Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher) :
    FolderGridCacheDataSource {
    private val _gridItemsCache = MutableStateFlow(emptyList<GridItem>())

    override val gridItemsCache = _gridItemsCache.asStateFlow()

    override fun insertGridItems(gridItems: List<GridItem>) {
        _gridItemsCache.update {
            gridItems
        }
    }

    override fun insertGridItem(gridItem: GridItem) {
        _gridItemsCache.update { currentGridCacheItems ->
            currentGridCacheItems + gridItem
        }
    }

    override suspend fun upsertGridItems(gridItems: List<GridItem>) {
        withContext(defaultDispatcher) {
            _gridItemsCache.update { currentGridCacheItems ->
                currentGridCacheItems.toMutableList().apply {
                    gridItems.forEach { gridItem ->
                        val index = indexOfFirst { it.id == gridItem.id }

                        if (index != -1) {
                            if (get(index) != gridItem) {
                                set(index, gridItem)
                            }
                        } else {
                            add(gridItem)
                        }
                    }
                }
            }
        }
    }
}
