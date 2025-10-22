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
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateApplicationInfoGridItemsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        withContext(ioDispatcher) {
            val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val applicationInfoGridItems =
                applicationInfoGridItemRepository.applicationInfoGridItems.first()

            val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

            val launcherActivityInfosMap =
                launcherAppsActivityInfos.associateBy { launcherAppsShortcutInfo -> launcherAppsShortcutInfo.packageName to launcherAppsShortcutInfo.serialNumber }

            val updatedApplicationInfoGridItems =
                applicationInfoGridItems.mapNotNull { applicationInfoGridItem ->
                    val key =
                        applicationInfoGridItem.packageName to applicationInfoGridItem.serialNumber

                    val matchingLauncherActivityInfo = launcherActivityInfosMap[key]

                    if (matchingLauncherActivityInfo != null) {
                        val icon = matchingLauncherActivityInfo.icon?.let { byteArray ->
                            fileManager.getAndUpdateFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                                name = matchingLauncherActivityInfo.packageName,
                                byteArray = byteArray,
                            )
                        }

                        applicationInfoGridItem.copy(
                            componentName = matchingLauncherActivityInfo.componentName,
                            icon = icon,
                            label = matchingLauncherActivityInfo.label,
                        )
                    } else {
                        deleteApplicationInfoGridItems.add(applicationInfoGridItem)

                        null
                    }
                }

            applicationInfoGridItemRepository.updateApplicationInfoGridItems(
                applicationInfoGridItems = updatedApplicationInfoGridItems,
            )

            applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
                applicationInfoGridItems = deleteApplicationInfoGridItems,
            )
        }
    }
}
