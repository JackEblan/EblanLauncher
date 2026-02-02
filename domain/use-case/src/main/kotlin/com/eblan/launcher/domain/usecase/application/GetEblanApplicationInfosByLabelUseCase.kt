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
package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanUserType
import com.eblan.launcher.domain.model.GetEblanApplicationInfosByLabel
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetEblanApplicationInfosByLabelUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        labelFlow: Flow<String>,
        eblanApplicationInfoTagFlow: Flow<Long?>,
    ): Flow<GetEblanApplicationInfosByLabel> = combine(
        eblanApplicationInfoRepository.eblanApplicationInfos,
        labelFlow,
        eblanApplicationInfoTagFlow,
    ) { eblanApplicationInfos, label, tagId ->
        val currentEblanApplicationInfos = if (tagId != null) {
            eblanApplicationInfoRepository.getEblanApplicationInfosByTagIdList(tagId = tagId)
        } else {
            eblanApplicationInfos
        }

        val groupedEblanApplicationInfos =
            currentEblanApplicationInfos.filter { eblanApplicationInfo ->
                !eblanApplicationInfo.isHidden && eblanApplicationInfo.label.contains(
                    other = label,
                    ignoreCase = true,
                )
            }.sortedWith(
                compareBy(
                    { it.serialNumber },
                    { it.label.lowercase() },
                ),
            ).groupBy { eblanApplicationInfo ->
                launcherAppsWrapper.getUser(serialNumber = eblanApplicationInfo.serialNumber)
            }

        val index = groupedEblanApplicationInfos.keys.toList().indexOfFirst { eblanUser ->
            eblanUser.eblanUserType == EblanUserType.Private
        }

        val privateEblanUser = groupedEblanApplicationInfos.keys.toList().getOrNull(index)

        GetEblanApplicationInfosByLabel(
            eblanApplicationInfos = groupedEblanApplicationInfos.filterKeys { eblanUser ->
                eblanUser != privateEblanUser
            },
            privateEblanUser = privateEblanUser,
            privateEblanApplicationInfos = groupedEblanApplicationInfos[privateEblanUser].orEmpty(),
        )
    }.flowOn(defaultDispatcher)
}
