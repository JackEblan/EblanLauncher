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
import com.eblan.launcher.domain.model.EblanApplicationComponent
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetEblanApplicationComponentUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<EblanApplicationComponent> {
        return combine(
            eblanApplicationInfoRepository.eblanApplicationInfos,
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos,
            eblanShortcutInfoRepository.eblanShortcutInfos,
        ) { eblanApplicationInfos, eblanAppWidgetProviderInfos, eblanShortcutInfos ->
            val sortedEblanApplicationInfos =
                eblanApplicationInfos.sortedBy { eblanApplicationInfo ->
                    eblanApplicationInfo.label?.lowercase()
                }

            val groupedEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfos.sortedBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo.label
                }.groupBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.eblanApplicationInfo
                }

            val groupedEblanShortcutInfos =
                eblanShortcutInfos.sortedBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo.label
                }.groupBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo
                }

            EblanApplicationComponent(
                eblanApplicationInfos = sortedEblanApplicationInfos,
                eblanAppWidgetProviderInfos = groupedEblanAppWidgetProviderInfos,
                eblanShortcutInfos = groupedEblanShortcutInfos,
            )
        }.flowOn(defaultDispatcher)
    }
}
