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
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
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
        withContext(ioDispatcher) {
            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfo(
                    serialNumber = 0L,
                    packageName = iconPackInfoPackageName,
                )

            if (iconPackInfoPackageName.isNotEmpty() && eblanApplicationInfo != null) {
                val appFilter =
                    iconPackManager.parseAppFilter(packageName = iconPackInfoPackageName)

                val iconPackDirectory = File(
                    fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                    iconPackInfoPackageName,
                ).apply { if (!exists()) mkdirs() }

                val installedPackageNames = launcherAppsWrapper.getActivityList()
                    .onEach { launcherAppsActivityInfo ->
                        ensureActive()

                        cacheIconPackFile(
                            appFilter = appFilter,
                            launcherAppsActivityInfo = launcherAppsActivityInfo,
                            iconPackInfoPackageName = iconPackInfoPackageName,
                            iconPackDirectory = iconPackDirectory,
                        )
                    }
                    .map { launcherAppsActivityInfo ->
                        ensureActive()

                        launcherAppsActivityInfo.packageName
                    }

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                    ),
                )

                iconPackDirectory.listFiles()
                    ?.filter { it.isFile && it.name !in installedPackageNames }
                    ?.forEach {
                        ensureActive()

                        it.delete()
                    }
            }
        }
    }

    private suspend fun cacheIconPackFile(
        appFilter: List<IconPackInfoComponent>,
        launcherAppsActivityInfo: LauncherAppsActivityInfo,
        iconPackInfoPackageName: String,
        iconPackDirectory: File,
    ) {
        val iconPackInfoComponent = appFilter.find { iconPackInfoComponent ->
            iconPackInfoComponent.component.contains(launcherAppsActivityInfo.componentName) ||
                iconPackInfoComponent.component.contains(
                    launcherAppsActivityInfo.packageName,
                )
        } ?: return

        val byteArray = iconPackManager.loadByteArrayFromIconPack(
            packageName = iconPackInfoPackageName,
            drawableName = iconPackInfoComponent.drawable,
        ) ?: return

        fileManager.updateAndGetFilePath(
            directory = iconPackDirectory,
            name = launcherAppsActivityInfo.packageName,
            byteArray = byteArray,
        )
    }
}
