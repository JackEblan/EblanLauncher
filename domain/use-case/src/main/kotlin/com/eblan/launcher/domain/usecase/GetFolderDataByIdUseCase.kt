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
import com.eblan.launcher.domain.model.FolderDataById
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetFolderDataByIdUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(id: String): FolderDataById? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            folderGridItemRepository.getFolderGridItemData(id = id)?.let { folderGridItemData ->
                val gridItemsSpanWithinBounds = folderGridItemData.gridItems.filter { gridItem ->
                    isGridItemSpanWithinBounds(
                        gridItem = gridItem,
                        rows = homeSettings.folderRows,
                        columns = homeSettings.folderColumns,
                    )
                }.groupBy { gridItem -> gridItem.page }

                FolderDataById(
                    id = id,
                    label = folderGridItemData.label,
                    gridItems = folderGridItemData.gridItems,
                    gridItemsByPage = gridItemsSpanWithinBounds,
                    pageCount = folderGridItemData.pageCount,
                )
            }
        }
    }
}
