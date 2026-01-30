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
package com.eblan.launcher.domain.usecase.iconpack

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateIconPackInfosUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackInfoPackageName: String) {
        if (iconPackInfoPackageName.isEmpty()) return

        withContext(ioDispatcher) {
            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                    serialNumber = 0L,
                    packageName = iconPackInfoPackageName,
                ).firstOrNull()

            if (eblanApplicationInfo != null) {
                val appFilter =
                    iconPackManager.parseAppFilter(packageName = iconPackInfoPackageName)

                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                ).apply { if (!exists()) mkdirs() }

                val installedPackageNames = buildList {
                    launcherAppsWrapper.getActivityList().forEach { launcherAppsActivityInfo ->
                        ensureActive()

                        cacheIconPackFile(
                            iconPackManager = iconPackManager,
                            appFilter = appFilter,
                            iconPackInfoPackageName = iconPackInfoPackageName,
                            iconPackInfoDirectory = iconPackDirectory,
                            componentName = launcherAppsActivityInfo.componentName,
                        )

                        add(launcherAppsActivityInfo.componentName.hashCode().toString())
                    }
                }

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                    ),
                )

                iconPackDirectory.listFiles()
                    ?.filter { it.isFile && it.name !in installedPackageNames }?.forEach {
                        ensureActive()

                        it.delete()
                    }
            }
        }
    }
}
