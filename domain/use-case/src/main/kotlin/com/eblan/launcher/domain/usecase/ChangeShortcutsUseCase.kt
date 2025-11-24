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
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ChangeShortcutsUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        packageName: String,
        launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) {
            return
        }

        withContext(ioDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

            val newEblanShortcutInfos = launcherAppsShortcutInfos.map { launcherAppsShortcutInfo ->
                ensureActive()

                val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                    fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                        name = launcherAppsShortcutInfo.shortcutId,
                        byteArray = byteArray,
                    )
                }

                EblanShortcutInfo(
                    shortcutId = launcherAppsShortcutInfo.shortcutId,
                    serialNumber = launcherAppsShortcutInfo.serialNumber,
                    packageName = launcherAppsShortcutInfo.packageName,
                    shortLabel = launcherAppsShortcutInfo.shortLabel,
                    longLabel = launcherAppsShortcutInfo.longLabel,
                    icon = icon,
                    shortcutQueryFlag = launcherAppsShortcutInfo.shortcutQueryFlag,
                    isEnabled = launcherAppsShortcutInfo.isEnabled,
                )
            }

            if (oldEblanShortcutInfos != newEblanShortcutInfos) {
                val eblanShortcutInfosToDelete =
                    oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

                eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                    eblanShortcutInfos = newEblanShortcutInfos,
                )

                eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                    eblanShortcutInfos = eblanShortcutInfosToDelete,
                )

                eblanShortcutInfosToDelete.forEach { eblanShortcutInfo ->
                    ensureActive()

                    val shortcutFile = File(
                        fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                        eblanShortcutInfo.shortcutId,
                    )

                    if (shortcutFile.exists()) {
                        shortcutFile.delete()
                    }
                }
            }
        }
    }
}
