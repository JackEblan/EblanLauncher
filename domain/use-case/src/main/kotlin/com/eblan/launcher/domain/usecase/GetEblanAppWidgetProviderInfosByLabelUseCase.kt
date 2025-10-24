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
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfoApplicationInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanAppWidgetProviderInfosByLabelUseCase @Inject constructor(
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<Map<EblanAppWidgetProviderInfoApplicationInfo, List<EblanAppWidgetProviderInfo>>> {
        return eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfos ->
            eblanAppWidgetProviderInfos.filter { eblanAppWidgetProviderInfo ->
                label.isNotBlank() && eblanAppWidgetProviderInfo.label.toString().contains(
                    other = label,
                    ignoreCase = true,
                )
            }.sortedBy { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.label
            }.groupBy { eblanAppWidgetProviderInfo ->
                EblanAppWidgetProviderInfoApplicationInfo(
                    icon = eblanAppWidgetProviderInfo.icon,
                    label = eblanAppWidgetProviderInfo.label,
                )
            }
        }.flowOn(defaultDispatcher)
    }
}
