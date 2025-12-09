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
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetEblanApplicationComponentUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<EblanApplicationComponent> {
        return combine(
            eblanApplicationInfoRepository.eblanApplicationInfos,
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos,
            eblanShortcutConfigRepository.eblanShortcutConfigs,
        ) { eblanApplicationInfos, eblanAppWidgetProviderInfos, eblanShortcutConfigs ->
            val groupedEblanApplicationInfos =
                eblanApplicationInfos.sortedBy { eblanApplicationInfo ->
                    eblanApplicationInfo.label?.lowercase()
                }.groupBy { eblanApplicationInfo ->
                    eblanApplicationInfo.serialNumber
                }

            val groupedEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfos.sortedBy { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.label.lowercase()
                }.groupBy { eblanAppWidgetProviderInfo ->
                    EblanApplicationInfoGroup(
                        packageName = eblanAppWidgetProviderInfo.packageName,
                        icon = eblanAppWidgetProviderInfo.icon,
                        label = eblanAppWidgetProviderInfo.label,
                    )
                }

            val groupedEblanShortcutConfigs =
                eblanShortcutConfigs.sortedBy { eblanShortcutConfig ->
                    eblanShortcutConfig.applicationLabel?.lowercase()
                }.groupBy { eblanShortcutConfig ->
                    eblanShortcutConfig.serialNumber
                }.mapValues { entry ->
                    entry.value.groupBy { eblanShortcutConfig ->
                        EblanApplicationInfoGroup(
                            packageName = eblanShortcutConfig.packageName,
                            icon = eblanShortcutConfig.applicationIcon,
                            label = eblanShortcutConfig.applicationLabel,
                        )
                    }
                }

            EblanApplicationComponent(
                eblanApplicationInfos = groupedEblanApplicationInfos,
                eblanAppWidgetProviderInfos = groupedEblanAppWidgetProviderInfos,
                eblanShortcutConfigs = groupedEblanShortcutConfigs,
            )
        }.flowOn(defaultDispatcher)
    }
}
