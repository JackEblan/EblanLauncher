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
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateShortcutInfoGridItem
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateShortcutInfoGridItemsByPackageNameUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        withContext(defaultDispatcher) {
            val updateShortcutInfoGridItems = mutableListOf<UpdateShortcutInfoGridItem>()

            val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            val launcherAppsShortcutInfos = launcherAppsWrapper.getPinnedShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            if (launcherAppsShortcutInfos != null) {
                shortcutInfoGridItems.forEach { shortcutInfoGridItem ->
                    val launcherAppsShortcutInfo =
                        launcherAppsShortcutInfos.find { launcherAppsShortcutInfo ->
                            launcherAppsShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId &&
                                    launcherAppsShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber
                        }

                    if (launcherAppsShortcutInfo != null) {
                        val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                            fileManager.getAndUpdateFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                                name = launcherAppsShortcutInfo.shortcutId,
                                byteArray = byteArray,
                            )
                        }

                        updateShortcutInfoGridItems.add(
                            UpdateShortcutInfoGridItem(
                                id = shortcutInfoGridItem.id,
                                shortLabel = launcherAppsShortcutInfo.shortLabel,
                                longLabel = launcherAppsShortcutInfo.longLabel,
                                isEnabled = launcherAppsShortcutInfo.isEnabled,
                                disabledMessage = launcherAppsShortcutInfo.disabledMessage,
                                icon = icon,
                            ),
                        )
                    } else {
                        deleteShortcutInfoGridItems.add(shortcutInfoGridItem)
                    }
                }

                shortcutInfoGridItemRepository.updateShortcutInfoGridItems(
                    updateShortcutInfoGridItems = updateShortcutInfoGridItems
                )

                shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = deleteShortcutInfoGridItems)
            }
        }
    }
}
