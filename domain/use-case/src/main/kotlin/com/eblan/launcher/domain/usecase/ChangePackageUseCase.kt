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
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val updateEblanAppWidgetProviderInfosByPackageNameUseCase: UpdateEblanAppWidgetProviderInfosByPackageNameUseCase,
    private val updateApplicationInfoGridItemsByPackageNameUseCase: UpdateApplicationInfoGridItemsByPackageNameUseCase,
    private val updateWidgetGridItemsByPackageNameUseCase: UpdateWidgetGridItemsByPackageNameUseCase,
    private val updateShortcutInfoGridItemsByPackageNameUseCase: UpdateShortcutInfoGridItemsByPackageNameUseCase,
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    private val updateEblanShortcutInfosByPackageNameUseCase: UpdateEblanShortcutInfosByPackageNameUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
            ensureActive()

            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

            val iconByteArray = packageManagerWrapper.getApplicationIcon(packageName = packageName)

            val icon = iconByteArray?.let { currentIconByteArray ->
                fileManager.getAndUpdateFilePath(
                    directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                    name = packageName,
                    byteArray = currentIconByteArray,
                )
            }

            val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

            val eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
                serialNumber = serialNumber,
                packageName = packageName
            )

            if (eblanApplicationInfo != null) {
                eblanApplicationInfoRepository.upsertEblanApplicationInfo(
                    eblanApplicationInfo = eblanApplicationInfo.copy(
                        componentName = componentName,
                        icon = icon,
                        label = label,
                    )
                )
            }

            updateEblanAppWidgetProviderInfosByPackageNameUseCase(packageName = packageName)

            updateEblanShortcutInfosByPackageNameUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateShortcutInfoGridItemsByPackageNameUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateApplicationInfoGridItemsByPackageNameUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateWidgetGridItemsByPackageNameUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateShortcutInfoGridItemsByPackageNameUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateIconPackInfoByPackageNameUseCase(packageName = packageName)
        }
    }
}
