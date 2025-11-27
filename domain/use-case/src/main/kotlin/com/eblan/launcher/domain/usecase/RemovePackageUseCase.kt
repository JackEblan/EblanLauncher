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
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
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
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            deleteEblanApplicationInfoFiles(
                packageName = packageName,
                serialNumber = serialNumber,
            )

            deleteEblanAppWidgetProviderInfoFiles(packageName = packageName)

            deleteEblaShortcutInfoFiles(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            deleteEblanShortcutConfigActivityFiles(
                serialNumber = serialNumber,
                packageName = packageName,
            )

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

            eblanShortcutConfigRepository.deleteEblanShortcutConfig(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            applicationInfoGridItemRepository.deleteApplicationInfoGridItem(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            widgetGridItemRepository.deleteWidgetGridItem(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            shortcutInfoGridItemRepository.deleteShortcutInfoGridItem(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun deleteEblanApplicationInfoFiles(
        packageName: String,
        serialNumber: Long,
    ) {
        val iconPackInfoPackageName =
            userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

        val isUnique =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()
                .none { eblanApplicationInfo ->
                    currentCoroutineContext().ensureActive()

                    eblanApplicationInfo.packageName == packageName &&
                        eblanApplicationInfo.serialNumber != serialNumber
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
    }

    private suspend fun deleteEblanAppWidgetProviderInfoFiles(packageName: String) {
        eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfosByPackageName(
            packageName = packageName,
        ).forEach { eblanAppWidgetProviderInfo ->
            currentCoroutineContext().ensureActive()

            val widgetFile = File(
                fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                eblanAppWidgetProviderInfo.componentName.replace("/", "-"),
            )

            if (widgetFile.exists()) {
                widgetFile.delete()
            }
        }
    }

    private suspend fun deleteEblaShortcutInfoFiles(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutInfoRepository.getEblanShortcutInfos(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { eblanShortcutInfo ->
            currentCoroutineContext().ensureActive()

            val isUnique = eblanShortcutInfoRepository.getEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            ).none { eblanShortcutInfo ->
                currentCoroutineContext().ensureActive()

                eblanShortcutInfo.packageName == packageName &&
                    eblanShortcutInfo.serialNumber != serialNumber
            }

            if (isUnique) {
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

    private suspend fun deleteEblanShortcutConfigActivityFiles(
        serialNumber: Long,
        packageName: String,
    ) {
        shortcutConfigGridItemRepository.getShortcutConfigGridItems(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { shortcutConfigGridItem ->
            val uriIcon = shortcutConfigGridItem.uriIcon

            if (uriIcon != null) {
                val uriIconFile = File(
                    fileManager.getFilesDirectory(FileManager.URIS_DIR),
                    uriIcon,
                )

                if (uriIconFile.exists()) {
                    uriIconFile.delete()
                }
            }
        }

        eblanShortcutConfigRepository.getEblanShortcutConfig(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { eblanShortcutConfigActivity ->
            currentCoroutineContext().ensureActive()

            val isUnique = eblanShortcutConfigRepository.getEblanShortcutConfig(
                serialNumber = serialNumber,
                packageName = packageName,
            ).none { eblanShortcutConfigActivity ->
                currentCoroutineContext().ensureActive()

                eblanShortcutConfigActivity.packageName == packageName &&
                    eblanShortcutConfigActivity.serialNumber != serialNumber
            }

            if (isUnique) {
                val shortcutFile = File(
                    fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIG_ACTIVITIES_DIR),
                    eblanShortcutConfigActivity.componentName,
                )

                if (shortcutFile.exists()) {
                    shortcutFile.delete()
                }
            }
        }
    }
}
