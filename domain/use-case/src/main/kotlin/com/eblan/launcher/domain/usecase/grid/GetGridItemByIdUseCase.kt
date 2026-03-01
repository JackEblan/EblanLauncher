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
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemCache
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetGridItemByIdUseCase @Inject constructor(
    private val gridRepository: GridRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(id: String): GridItem? = withContext(defaultDispatcher) {
        val folderGridItems = folderGridItemRepository.folderGridItemWrappers.first()
            .flatMap { folderGridItemWrapper ->
                folderGridItemWrapper.applicationInfoGridItems.map { gridItem ->
                    GridItem(
                        id = gridItem.id,
                        page = gridItem.page,
                        startColumn = gridItem.startColumn,
                        startRow = gridItem.startRow,
                        columnSpan = gridItem.columnSpan,
                        rowSpan = gridItem.rowSpan,
                        data = GridItemData.ApplicationInfo(
                            serialNumber = gridItem.serialNumber,
                            componentName = gridItem.componentName,
                            packageName = gridItem.packageName,
                            icon = gridItem.icon,
                            label = gridItem.label,
                            customIcon = gridItem.customIcon,
                            customLabel = gridItem.customLabel,
                            index = gridItem.index,
                            folderId = gridItem.folderId,
                        ),
                        associate = gridItem.associate,
                        override = gridItem.override,
                        gridItemSettings = gridItem.gridItemSettings,
                        doubleTap = gridItem.doubleTap,
                        swipeUp = gridItem.swipeUp,
                        swipeDown = gridItem.swipeDown,
                    )
                }
            }

        val gridItems = gridRepository.gridItems.first() + folderGridItems

        gridItems.find { gridItem ->
            gridItem.id == id
        }
    }
}
