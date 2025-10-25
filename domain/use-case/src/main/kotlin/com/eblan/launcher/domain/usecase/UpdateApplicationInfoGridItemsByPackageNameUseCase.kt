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
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateApplicationInfoGridItemsByPackageNameUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        withContext(defaultDispatcher) {
            val updateApplicationInfoGridItems = mutableListOf<UpdateApplicationInfoGridItem>()

            val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val applicationInfoGridItems =
                applicationInfoGridItemRepository.getApplicationInfoGridItems(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val launcherAppsActivityInfos =
                launcherAppsWrapper.getActivityList().filter { launcherAppsActivityInfo ->
                    launcherAppsActivityInfo.serialNumber == serialNumber &&
                        launcherAppsActivityInfo.packageName == packageName
                }

            applicationInfoGridItems.forEach { applicationInfoGridItem ->
                val launcherAppsActivityInfo =
                    launcherAppsActivityInfos.find { launcherAppsActivityInfo ->
                        launcherAppsActivityInfo.packageName == applicationInfoGridItem.packageName &&
                            launcherAppsActivityInfo.serialNumber == applicationInfoGridItem.serialNumber
                    }

                if (launcherAppsActivityInfo != null) {
                    val icon = launcherAppsActivityInfo.icon?.let { byteArray ->
                        fileManager.getFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                            name = launcherAppsActivityInfo.packageName,
                            byteArray = byteArray,
                        )
                    }

                    updateApplicationInfoGridItems.add(
                        UpdateApplicationInfoGridItem(
                            id = applicationInfoGridItem.id,
                            componentName = launcherAppsActivityInfo.componentName,
                            icon = icon,
                            label = launcherAppsActivityInfo.label,
                        ),
                    )
                } else {
                    deleteApplicationInfoGridItems.add(applicationInfoGridItem)
                }
            }

            applicationInfoGridItemRepository.updateApplicationInfoGridItems(
                updateApplicationInfoGridItems = updateApplicationInfoGridItems,
            )

            applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
                applicationInfoGridItems = deleteApplicationInfoGridItems,
            )
        }
    }
}
