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
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackInfosUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackInfoPackageName: String) {
        withContext(ioDispatcher) {
            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = iconPackInfoPackageName)

            if (iconPackInfoPackageName.isNotEmpty() && eblanApplicationInfo != null) {
                val appFilter =
                    iconPackManager.parseAppFilter(iconPackInfoPackageName = iconPackInfoPackageName)

                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                ).apply { if (!exists()) mkdirs() }

                val installedPackageNames = launcherAppsWrapper.getActivityList()
                    .mapNotNull { eblanLauncherActivityInfo ->
                        val entry = appFilter.entries.find { (component, _) ->
                            component.contains(eblanLauncherActivityInfo.packageName)
                        } ?: return@mapNotNull null

                        val byteArray = iconPackManager.loadByteArrayFromIconPack(
                            packageName = iconPackInfoPackageName,
                            drawableName = entry.value,
                        ) ?: return@mapNotNull null

                        fileManager.getAndUpdateFilePath(
                            directory = iconPackDirectory,
                            name = eblanLauncherActivityInfo.packageName,
                            byteArray = byteArray,
                        )

                        eblanLauncherActivityInfo.packageName
                    }
                    .toSet()

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                    ),
                )

                iconPackDirectory.listFiles()
                    ?.filter { it.isFile && it.name !in installedPackageNames }
                    ?.forEach { it.delete() }
            }
        }
    }
}
