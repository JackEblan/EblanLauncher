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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.FolderGridCacheRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MoveGridItemOutsideFolderUseCase @Inject constructor(
    private val folderGridCacheRepository: FolderGridCacheRepository,
    private val gridCacheRepository: GridCacheRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        folderId: String,
        movingGridItem: GridItem,
        gridItems: List<GridItem>,
    ) {
        return withContext(defaultDispatcher) {
            val folderGridItems =
                folderGridCacheRepository.gridItemsCache.first().toMutableList().apply {
                    removeIf { gridItem ->
                        gridItem.id == movingGridItem.id
                    }
                }

            val folderGridItem = gridItems.find { gridItem ->
                gridItem.id == folderId && gridItem.data is GridItemData.Folder
            }

            if (folderGridItem != null) {
                val data = folderGridItem.data as GridItemData.Folder

                gridCacheRepository.insertGridItems(gridItems = gridItems)

                gridCacheRepository.updateGridItemData(
                    id = folderId,
                    data = data.copy(gridItems = folderGridItems),
                )
            }
        }
    }
}
