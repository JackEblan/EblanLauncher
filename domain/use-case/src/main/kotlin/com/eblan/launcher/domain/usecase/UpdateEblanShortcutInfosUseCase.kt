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
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateEblanShortcutInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) {
            return
        }

        withContext(defaultDispatcher) {
            val oldEblanApplicationInfos =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()

            val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

            val newEblanShortcutInfos =
                launcherAppsWrapper.getShortcuts()?.mapNotNull { launcherAppsShortcutInfo ->
                    val eblanApplicationInfo =
                        oldEblanApplicationInfos.find { eblanApplicationInfo ->
                            eblanApplicationInfo.packageName == launcherAppsShortcutInfo.packageName
                        }

                    if (eblanApplicationInfo != null) {
                        val icon = fileManager.getAndUpdateFilePath(
                            directory = fileManager.getDirectory(FileManager.SHORTCUTS_DIR),
                            name = launcherAppsShortcutInfo.shortcutId,
                            byteArray = launcherAppsShortcutInfo.icon,
                        )

                        EblanShortcutInfo(
                            shortcutId = launcherAppsShortcutInfo.shortcutId,
                            packageName = launcherAppsShortcutInfo.packageName,
                            shortLabel = launcherAppsShortcutInfo.shortLabel,
                            longLabel = launcherAppsShortcutInfo.longLabel,
                            eblanApplicationInfo = eblanApplicationInfo,
                            icon = icon,
                        )
                    } else {
                        null
                    }
                }

            if (newEblanShortcutInfos != null && oldEblanShortcutInfos != newEblanShortcutInfos) {
                val eblanShortcutInfosToDelete =
                    oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

                eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                    eblanShortcutInfos = newEblanShortcutInfos,
                )

                eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                    eblanShortcutInfos = eblanShortcutInfosToDelete,
                )

                eblanShortcutInfosToDelete.forEach { eblanShortcutInfo ->
                    fileManager.deleteFile(
                        directory = fileManager.getDirectory(FileManager.SHORTCUTS_DIR),
                        name = eblanShortcutInfo.shortcutId,
                    )
                }
            }
        }
    }
}
