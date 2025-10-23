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
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val updateEblanAppWidgetProviderInfosByPackageNameUseCase: UpdateEblanAppWidgetProviderInfosByPackageNameUseCase,
    private val updateApplicationInfoGridItemsByPackageNameUseCase: UpdateApplicationInfoGridItemsByPackageNameUseCase,
    private val updateWidgetGridItemsByPackageNameUseCase: UpdateWidgetGridItemsByPackageNameUseCase,
    private val updateShortcutInfoGridItemsByPackageNameUseCase: UpdateShortcutInfoGridItemsByPackageNameUseCase,
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
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

            val eblanApplicationInfo = EblanApplicationInfo(
                serialNumber = serialNumber,
                componentName = componentName,
                packageName = packageName,
                icon = icon,
                label = label,
            )

            launch {
                eblanApplicationInfoRepository.upsertEblanApplicationInfo(eblanApplicationInfo = eblanApplicationInfo)
            }

            launch {
                updateEblanAppWidgetProviderInfosByPackageNameUseCase(packageName = packageName)
            }

            launch {
                updateApplicationInfoGridItemsByPackageNameUseCase(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )
            }

            launch {
                updateWidgetGridItemsByPackageNameUseCase(packageName = packageName)
            }

            launch {
                updateShortcutInfoGridItemsByPackageNameUseCase(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )
            }

            launch {
                updateIconPackInfoByPackageNameUseCase(packageName = packageName)
            }
        }
    }
}
