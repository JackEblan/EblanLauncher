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
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RemovePackageUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            val iconPackInfoPackageName =
                userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

            val isUnique =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()
                    .none { eblanApplicationInfo ->
                        ensureActive()

                        eblanApplicationInfo.packageName == packageName && eblanApplicationInfo.serialNumber != serialNumber
                    }

            if (isUnique) {
                val iconFile = File(
                    fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                    packageName,
                )

                if (iconFile.exists()) {
                    iconFile.delete()
                }

                val iconPacksDirectory = File(
                    fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                )

                val iconPackFile = File(iconPacksDirectory, packageName)

                if (iconPackFile.exists()) {
                    iconPackFile.delete()
                }
            }

            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfosByPackageName(
                packageName = packageName,
            ).forEach { eblanAppWidgetProviderInfo ->
                ensureActive()

                val widgetFile = File(
                    fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                    eblanAppWidgetProviderInfo.className,
                )

                if (widgetFile.exists()) {
                    widgetFile.delete()
                }
            }

            eblanShortcutInfoRepository.getEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            ).forEach { eblanShortcutInfo ->
                ensureActive()

                val shortcutFile = File(
                    fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                    eblanShortcutInfo.shortcutId,
                )

                if (shortcutFile.exists()) {
                    shortcutFile.delete()
                }
            }

            eblanApplicationInfoRepository.deleteEblanApplicationInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfoByPackageName(
                packageName = packageName,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }
}
