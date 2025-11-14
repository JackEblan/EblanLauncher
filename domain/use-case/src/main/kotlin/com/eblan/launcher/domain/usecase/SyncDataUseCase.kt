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
import com.eblan.launcher.domain.framework.NotificationManagerWrapper
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val updateEblanApplicationInfosUseCase: UpdateEblanApplicationInfosUseCase,
    private val updateEblanAppWidgetProviderInfosUseCase: UpdateEblanAppWidgetProviderInfosUseCase,
    private val updateEblanShortcutInfosUseCase: UpdateEblanShortcutInfosUseCase,
    private val notificationManagerWrapper: NotificationManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(isManualSyncData: Boolean) {
        withContext(defaultDispatcher) {
            suspend fun syncData() {
                joinAll(
                    launch {
                        updateEblanApplicationInfosUseCase()

                        updateEblanAppWidgetProviderInfosUseCase()
                    },
                    launch {
                        updateEblanShortcutInfosUseCase()
                    },
                )
            }

            when {
                isManualSyncData -> {
                    syncData()
                }

                userDataRepository.userData.first().experimentalSettings.syncData -> {
                    try {
                        notificationManagerWrapper.notifySyncData()

                        syncData()
                    } finally {
                        notificationManagerWrapper.cancelSyncData()
                    }
                }
            }
        }
    }
}
