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
import com.eblan.launcher.domain.model.EblanShortcutConfigActivity
import com.eblan.launcher.domain.repository.EblanShortcutConfigActivityRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateEblanShortcutConfigActivitiesUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val eblanShortcutConfigActivityRepository: EblanShortcutConfigActivityRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val oldEblanShortcutConfigActivities =
                eblanShortcutConfigActivityRepository.getEblanShortcutConfigActivity(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val newEblanShortcutConfigActivities =
                launcherAppsWrapper.getShortcutConfigActivityList(
                    serialNumber = serialNumber,
                    packageName = packageName,
                ).map { launcherAppsActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    val icon = launcherAppsActivityInfo.icon?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIG_ACTIVITIES_DIR),
                            name = launcherAppsActivityInfo.componentName.replace(
                                "/",
                                "-",
                            ),
                            byteArray = byteArray,
                        )
                    }

                    EblanShortcutConfigActivity(
                        componentName = launcherAppsActivityInfo.componentName,
                        packageName = launcherAppsActivityInfo.packageName,
                        serialNumber = launcherAppsActivityInfo.serialNumber,
                        icon = icon,
                        label = launcherAppsActivityInfo.label,
                    )
                }

            if (oldEblanShortcutConfigActivities != newEblanShortcutConfigActivities) {
                val eblanShortcutConfigActivitiesToDelete =
                    oldEblanShortcutConfigActivities - newEblanShortcutConfigActivities.toSet()

                eblanShortcutConfigActivityRepository.upsertEblanShortcutConfigActivities(
                    eblanShortcutConfigActivities = newEblanShortcutConfigActivities,
                )

                eblanShortcutConfigActivityRepository.deleteEblanShortcutConfigActivities(
                    eblanShortcutConfigActivities = oldEblanShortcutConfigActivities,
                )

                eblanShortcutConfigActivitiesToDelete.forEach { eblanShortcutConfigActivityToDelete ->
                    currentCoroutineContext().ensureActive()

                    val isUnique =
                        newEblanShortcutConfigActivities.none { newEblanShortcutConfigActivity ->
                            newEblanShortcutConfigActivity.packageName == eblanShortcutConfigActivityToDelete.packageName &&
                                newEblanShortcutConfigActivity.serialNumber != eblanShortcutConfigActivityToDelete.serialNumber
                        }

                    if (isUnique) {
                        val icon = File(
                            fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIG_ACTIVITIES_DIR),
                            eblanShortcutConfigActivityToDelete.componentName,
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
