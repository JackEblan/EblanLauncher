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
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdatePageItemsUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val updateGridItemsUseCase: UpdateGridItemsUseCase,
    private val deleteGridItemsUseCase: DeleteGridItemsUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        initialPage: Int,
        pageItems: List<PageItem>,
        pageItemsToDelete: List<PageItem>,
    ) {
        withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            pageItemsToDelete.forEach { pageItem ->
                deleteGridItemsUseCase(gridItems = pageItem.gridItems)
            }

            val gridItems = pageItems.mapIndexed { index, pageItem ->
                pageItem.gridItems.map { gridItem ->
                    gridItem.copy(page = index)
                }
            }.flatten()

            val newInitialPage = pageItems.indexOfFirst { pageItem -> pageItem.id == initialPage }

            if (newInitialPage != -1) {
                userDataRepository.updateHomeSettings(homeSettings = homeSettings.copy(initialPage = newInitialPage))
            }

            userDataRepository.updateHomeSettings(homeSettings = homeSettings.copy(pageCount = pageItems.size))

            updateGridItemsUseCase(gridItems = gridItems)
        }
    }
}
