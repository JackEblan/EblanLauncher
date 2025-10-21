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
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateEblanApplicationInfosUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val updateIconPackInfosUseCase: UpdateIconPackInfosUseCase,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            val oldEblanApplicationInfos =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()

            val newEblanApplicationInfos =
                launcherAppsWrapper.getActivityList().map { eblanLauncherActivityInfo ->
                    val icon = eblanLauncherActivityInfo.icon?.let { currentIcon ->
                        fileManager.getAndUpdateFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                            name = eblanLauncherActivityInfo.packageName,
                            byteArray = currentIcon,
                        )
                    }

                    EblanApplicationInfo(
                        serialNumber = eblanLauncherActivityInfo.serialNumber,
                        componentName = eblanLauncherActivityInfo.componentName,
                        packageName = eblanLauncherActivityInfo.packageName,
                        icon = icon,
                        label = eblanLauncherActivityInfo.label,
                    )
                }

            if (oldEblanApplicationInfos != newEblanApplicationInfos) {
                val eblanApplicationInfosToDelete =
                    oldEblanApplicationInfos - newEblanApplicationInfos.toSet()

                eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = newEblanApplicationInfos)

                eblanApplicationInfoRepository.deleteEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfosToDelete)

                eblanApplicationInfosToDelete.forEach { eblanApplicationInfo ->
                    val icon = File(
                        fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        eblanApplicationInfo.packageName,
                    )

                    if (icon.exists()) {
                        icon.delete()
                    }
                }

                val iconPackInfoPackageName =
                    userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

                updateIconPackInfosUseCase(packageName = iconPackInfoPackageName)
            }
        }
    }
}
