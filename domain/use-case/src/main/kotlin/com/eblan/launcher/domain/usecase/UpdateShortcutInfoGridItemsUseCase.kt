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
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateShortcutInfoGridItemsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        withContext(ioDispatcher) {
            val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

            val shortcutInfoGridItems = shortcutInfoGridItemRepository.shortcutInfoGridItems.first()

            val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts()

            if (launcherAppsShortcutInfos != null) {
                val launcherShortcutInfosMap =
                    launcherAppsShortcutInfos.associateBy { launcherAppsShortcutInfo -> launcherAppsShortcutInfo.shortcutId to launcherAppsShortcutInfo.serialNumber }

                val updatedShortcutInfoGridItems =
                    shortcutInfoGridItems.mapNotNull { shortcutInfoGridItem ->
                        val key =
                            shortcutInfoGridItem.shortcutId to shortcutInfoGridItem.serialNumber

                        val matchingLauncherShortcutInfo = launcherShortcutInfosMap[key]

                        if (matchingLauncherShortcutInfo != null) {
                            val icon = matchingLauncherShortcutInfo.icon?.let { byteArray ->
                                fileManager.getAndUpdateFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                                    name = matchingLauncherShortcutInfo.shortcutId,
                                    byteArray = byteArray,
                                )
                            }

                            shortcutInfoGridItem.copy(
                                shortLabel = matchingLauncherShortcutInfo.shortLabel,
                                longLabel = matchingLauncherShortcutInfo.longLabel,
                                icon = icon,
                            )
                        } else {
                            deleteShortcutInfoGridItems.add(shortcutInfoGridItem)

                            null
                        }
                    }

                shortcutInfoGridItemRepository.updateShortcutInfoGridItems(shortcutInfoGridItems = updatedShortcutInfoGridItems)

                shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = deleteShortcutInfoGridItems)
            }
        }
    }
}
