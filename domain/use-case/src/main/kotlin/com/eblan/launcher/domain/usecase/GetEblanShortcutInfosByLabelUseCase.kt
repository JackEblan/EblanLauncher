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
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanShortcutInfosByLabelUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(label: String): Flow<Map<EblanApplicationInfo, List<EblanShortcutInfo>>> {
        return eblanShortcutInfoRepository.eblanShortcutInfos.map { eblanShortcutInfos ->
            eblanShortcutInfos
                .filter { eblanShortcutInfo ->
                    val eblanApplicationInfoLabel =
                        eblanShortcutInfo.eblanApplicationInfo.label

                    eblanApplicationInfoLabel != null &&
                            label.isNotBlank() &&
                            eblanApplicationInfoLabel.contains(
                                other = label,
                                ignoreCase = true,
                            )
                }
                .sortedBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo.label
                }.groupBy { eblanShortcutInfo ->
                    eblanShortcutInfo.eblanApplicationInfo
                }
        }.flowOn(defaultDispatcher)
    }
}
