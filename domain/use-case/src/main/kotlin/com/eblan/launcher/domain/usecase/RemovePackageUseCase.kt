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
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
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
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val iconPackInfoPackageName =
                userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

            val isUnique =
                eblanApplicationInfoRepository.eblanApplicationInfos.first()
                    .none { eblanApplicationInfo ->
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
                val widgetFile = File(
                    fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                    eblanAppWidgetProviderInfo.className,
                )

                if (widgetFile.exists()) {
                    widgetFile.delete()
                }
            }

            eblanApplicationInfoRepository.deleteEblanApplicationInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfoByPackageName(
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
}
