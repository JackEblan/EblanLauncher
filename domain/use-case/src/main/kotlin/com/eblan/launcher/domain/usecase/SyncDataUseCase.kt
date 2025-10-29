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
    private val updateShortcutInfoGridItemsUseCase: UpdateShortcutInfoGridItemsUseCase,
    private val updateApplicationInfoGridItemsUseCase: UpdateApplicationInfoGridItemsUseCase,
    private val updateWidgetGridItemsUseCase: UpdateWidgetGridItemsUseCase,
    private val notificationManagerWrapper: NotificationManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        if (!userDataRepository.userData.first().experimentalSettings.syncData) return

        withContext(defaultDispatcher) {
            notificationManagerWrapper.notifySyncData(
                contentTitle = "Syncing data",
                contentText = "Editing grid items may cause unsaved changes",
            )

            try {
                joinAll(
                    launch {
                        updateEblanApplicationInfosUseCase()

                        updateEblanAppWidgetProviderInfosUseCase()
                    },
                    launch {
                        updateShortcutInfoGridItemsUseCase()
                    },
                    launch {
                        updateApplicationInfoGridItemsUseCase()

                        updateWidgetGridItemsUseCase()

                        updateShortcutInfoGridItemsUseCase()
                    },
                )
            } finally {
                notificationManagerWrapper.cancelSyncData()
            }
        }
    }
}
