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
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.PopupGridItem
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetEblanShortcutInfosByPackageNameUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        showPopupGridItemMenu: Boolean,
        packageName: String?,
    ): PopupGridItem? {
        return withContext(defaultDispatcher) {
            val eblanShortcutInfosByPackageName =
                if (packageName != null && launcherAppsWrapper.hasShortcutHostPermission) {
                    eblanShortcutInfoRepository.getEblanShortcutInfoByPackageName(
                        packageName = packageName,
                    )
                } else {
                    emptyList()
                }

            PopupGridItem(
                showPopupGridItemMenu = showPopupGridItemMenu,
                eblanShortcutInfosByPackageName = eblanShortcutInfosByPackageName,
            )
        }
    }
}
