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
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RemovePackageUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val userDataRepository: UserDataRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(packageName: String) {
        withContext(defaultDispatcher) {
            val iconPackInfoPackageName =
                userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

            eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
                packageName = packageName,
            )

            val iconFile = File(
                fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                packageName,
            )

            if (iconFile.exists()) {
                iconFile.delete()
            }

            appWidgetManagerWrapper.getInstalledProviders()
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }.forEach { appWidgetManagerAppWidgetProviderInfo ->
                    val widgetFile = File(
                        fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                        appWidgetManagerAppWidgetProviderInfo.className,
                    )

                    if (widgetFile.exists()) {
                        widgetFile.delete()
                    }
                }

            val iconPacksDirectory = File(
                fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                iconPackInfoPackageName,
            )

            val iconPackFile = File(iconPacksDirectory, packageName)

            if (iconPackFile.exists()) {
                iconPacksDirectory.delete()
            }
        }
    }
}
