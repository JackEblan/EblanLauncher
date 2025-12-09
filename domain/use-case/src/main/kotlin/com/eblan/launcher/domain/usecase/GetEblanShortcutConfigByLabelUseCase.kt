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
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanShortcutConfigByLabelUseCase @Inject constructor(
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<Map<EblanApplicationInfoGroup, List<EblanShortcutConfig>>> {
        return eblanShortcutConfigRepository.eblanShortcutConfigs.map { eblanShortcutConfigs ->
            eblanShortcutConfigs.sortedBy { eblanShortcutConfig ->
                eblanShortcutConfig.applicationLabel?.lowercase()
            }.filter { eblanShortcutConfig ->
                label.isNotBlank() && eblanShortcutConfig.applicationLabel.toString().contains(
                    other = label,
                    ignoreCase = true,
                )
            }.groupBy { eblanAppWidgetProviderInfo ->
                EblanApplicationInfoGroup(
                    packageName = eblanAppWidgetProviderInfo.packageName,
                    icon = eblanAppWidgetProviderInfo.activityIcon,
                    label = eblanAppWidgetProviderInfo.applicationLabel,
                )
            }
        }.flowOn(defaultDispatcher)
    }
}
