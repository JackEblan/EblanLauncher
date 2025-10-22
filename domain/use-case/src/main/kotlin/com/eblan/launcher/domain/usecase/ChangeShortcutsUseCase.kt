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
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeShortcutsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
        launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>,
    ) = withContext(defaultDispatcher) {

        val launcherAppsShortcutInfoIds = launcherAppsShortcutInfos.map { it.shortcutId }.toSet()

        val launcherAppsShortcutInfoMap =
            launcherAppsWrapper.getShortcuts(serialNumber = serialNumber)
                ?.groupBy { launcherAppsShortcutInfo -> launcherAppsShortcutInfo.packageName }
                ?: emptyMap()

        launcherAppsShortcutInfos.forEach { launcherAppsShortcutInfo ->
            shortcutInfoGridItemRepository.getShortcutInfoGridItems(packageName = packageName)
                .forEach { shortcutInfoGridItem ->
                    launcherAppsShortcutInfoMap[launcherAppsShortcutInfo.packageName]?.forEach { completeLauncherAppsShortcutInfo ->
                        val icon = completeLauncherAppsShortcutInfo.icon?.let { byteArray ->
                            fileManager.getAndUpdateFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                                name = completeLauncherAppsShortcutInfo.shortcutId,
                                byteArray = byteArray,
                            )
                        }

                        shortcutInfoGridItemRepository.updateShortcutInfoGridItem(
                            shortcutInfoGridItem.copy(
                                serialNumber = completeLauncherAppsShortcutInfo.serialNumber,
                                shortLabel = completeLauncherAppsShortcutInfo.shortLabel,
                                longLabel = completeLauncherAppsShortcutInfo.longLabel,
                                icon = icon,
                            )
                        )
                    }
                }
        }

        shortcutInfoGridItemRepository.getShortcutInfoGridItems(packageName)
            .filter { it.shortcutId !in launcherAppsShortcutInfoIds }
            .forEach { shortcutInfoGridItemRepository.deleteShortcutInfoGridItem(it) }
    }
}
