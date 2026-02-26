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
import com.eblan.launcher.domain.model.GridItemData.Folder
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min

class GetFolderGridItemsUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<GridItem>> =
        folderGridItemRepository.folderGridItemWrappers.map { folderGridItemWrappers ->
            folderGridItemWrappers.map { folderGridItemWrapper ->
                val maxColumns = 5
                val maxRows = 4

                val maxItemsPerPage = maxColumns * maxRows

                val gridItemsByPage =
                    folderGridItemWrapper.applicationInfoFolderGridItems.sortedBy { it.index }
                        .chunked(maxItemsPerPage)
                        .mapIndexed { pageIndex, pageItems -> pageIndex to pageItems }
                        .toMap()
                val firstPageGridItems = gridItemsByPage[0] ?: emptyList()

                val columns = min(maxColumns, firstPageGridItems.size)
                val rows = min(maxRows, ceil(firstPageGridItems.size / columns.toDouble()).toInt())

                val data = Folder(
                    id = folderGridItemWrapper.folderGridItem.id,
                    label = folderGridItemWrapper.folderGridItem.label,
                    gridItems = folderGridItemWrapper.applicationInfoFolderGridItems,
                    gridItemsByPage = gridItemsByPage,
                    icon = folderGridItemWrapper.folderGridItem.icon,
                    columns = columns,
                    rows = rows,
                )

                GridItem(
                    id = folderGridItemWrapper.folderGridItem.id,
                    page = folderGridItemWrapper.folderGridItem.page,
                    startColumn = folderGridItemWrapper.folderGridItem.startColumn,
                    startRow = folderGridItemWrapper.folderGridItem.startRow,
                    columnSpan = folderGridItemWrapper.folderGridItem.columnSpan,
                    rowSpan = folderGridItemWrapper.folderGridItem.rowSpan,
                    data = data,
                    associate = folderGridItemWrapper.folderGridItem.associate,
                    override = folderGridItemWrapper.folderGridItem.override,
                    gridItemSettings = folderGridItemWrapper.folderGridItem.gridItemSettings,
                    doubleTap = folderGridItemWrapper.folderGridItem.doubleTap,
                    swipeUp = folderGridItemWrapper.folderGridItem.swipeUp,
                    swipeDown = folderGridItemWrapper.folderGridItem.swipeDown,
                )
            }
        }.flowOn(defaultDispatcher)
}
