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
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateEblanShortcutConfigActivitiesUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
        icon: String?,
        label: String?,
    ) {
        withContext(ioDispatcher) {
            val oldEblanShortcutConfigs =
                eblanShortcutConfigRepository.getEblanShortcutConfig(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val newEblanShortcutConfigs =
                launcherAppsWrapper.getShortcutConfigActivityList(
                    serialNumber = serialNumber,
                    packageName = packageName,
                ).map { launcherAppsInfo ->
                    currentCoroutineContext().ensureActive()

                    val activityIcon = launcherAppsInfo.activityIcon?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIG_ACTIVITIES_DIR),
                            name = launcherAppsInfo.componentName.replace(
                                "/",
                                "-",
                            ),
                            byteArray = byteArray,
                        )
                    }

                    EblanShortcutConfig(
                        componentName = launcherAppsInfo.componentName,
                        packageName = launcherAppsInfo.packageName,
                        serialNumber = launcherAppsInfo.serialNumber,
                        activityIcon = activityIcon,
                        activityLabel = launcherAppsInfo.activityLabel,
                        applicationIcon = icon,
                        applicationLabel = label,
                    )
                }

            if (oldEblanShortcutConfigs != newEblanShortcutConfigs) {
                val eblanShortcutConfigActivitiesToDelete =
                    oldEblanShortcutConfigs - newEblanShortcutConfigs.toSet()

                eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
                    eblanShortcutConfigs = newEblanShortcutConfigs,
                )

                eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
                    eblanShortcutConfigs = oldEblanShortcutConfigs,
                )

                eblanShortcutConfigActivitiesToDelete.forEach { eblanShortcutConfigToDelete ->
                    currentCoroutineContext().ensureActive()

                    val isUnique =
                        newEblanShortcutConfigs.none { newEblanShortcutConfig ->
                            newEblanShortcutConfig.packageName == eblanShortcutConfigToDelete.packageName &&
                                newEblanShortcutConfig.serialNumber != eblanShortcutConfigToDelete.serialNumber
                        }

                    if (isUnique) {
                        val icon = File(
                            fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIG_ACTIVITIES_DIR),
                            eblanShortcutConfigToDelete.componentName,
                        )

                        if (icon.exists()) {
                            icon.delete()
                        }
                    }
                }
            }
        }
    }
}
