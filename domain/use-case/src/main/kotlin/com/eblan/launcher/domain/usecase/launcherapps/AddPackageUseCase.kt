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
package com.eblan.launcher.domain.usecase.launcherapps

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.iconpack.cacheIconPackFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AddPackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
            val userData = userDataRepository.userData.first()

            if (!userData.experimentalSettings.syncData) return@withContext

            val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityList(
                serialNumber = serialNumber,
                packageName = packageName,
            ).onEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                addEblanApplicationInfo(
                    componentName = launcherAppsActivityInfo.componentName,
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    packageName = launcherAppsActivityInfo.packageName,
                    icon = launcherAppsActivityInfo.activityIcon,
                    label = launcherAppsActivityInfo.activityLabel,
                    lastUpdateTime = launcherAppsActivityInfo.lastUpdateTime,
                )
            }

            addEblanAppWidgetProviderInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            addEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            addEblanShortcutConfigs(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            addIconPackInfos(
                iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                launcherAppsActivityInfos = launcherAppsActivityInfosByPackageName,
            )
        }
    }

    private suspend fun addEblanApplicationInfo(
        componentName: String,
        serialNumber: Long,
        packageName: String,
        icon: String?,
        label: String,
        lastUpdateTime: Long,
    ) {
        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
            eblanApplicationInfo = EblanApplicationInfo(
                componentName = componentName,
                serialNumber = serialNumber,
                packageName = packageName,
                icon = icon,
                label = label,
                customIcon = null,
                customLabel = null,
                isHidden = false,
                lastUpdateTime = lastUpdateTime,
                index = -1,
            ),
        )
    }

    private suspend fun addEblanAppWidgetProviderInfos(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.serialNumber == serialNumber &&
                    appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.map { appWidgetManagerAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val componentName =
                    packageManagerWrapper.getComponentName(packageName = appWidgetManagerAppWidgetProviderInfo.packageName)

                val icon = if (componentName != null) {
                    val file = File(
                        directory,
                        componentName.hashCode().toString(),
                    )

                    file.absolutePath
                } else {
                    val file = File(
                        directory,
                        appWidgetManagerAppWidgetProviderInfo.packageName.hashCode().toString(),
                    )

                    packageManagerWrapper.getApplicationIcon(
                        packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                        file = file,
                    )
                }

                EblanAppWidgetProviderInfo(
                    componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
                    serialNumber = serialNumber,
                    configure = appWidgetManagerAppWidgetProviderInfo.configure,
                    packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                    targetCellWidth = appWidgetManagerAppWidgetProviderInfo.targetCellWidth,
                    targetCellHeight = appWidgetManagerAppWidgetProviderInfo.targetCellHeight,
                    minWidth = appWidgetManagerAppWidgetProviderInfo.minWidth,
                    minHeight = appWidgetManagerAppWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetManagerAppWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetManagerAppWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetManagerAppWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = appWidgetManagerAppWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = appWidgetManagerAppWidgetProviderInfo.maxResizeHeight,
                    preview = appWidgetManagerAppWidgetProviderInfo.preview,
                    applicationIcon = icon,
                    applicationLabel = packageManagerWrapper.getApplicationLabel(
                        packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                    ).toString(),
                    lastUpdateTime = appWidgetManagerAppWidgetProviderInfo.lastUpdateTime,
                    label = appWidgetManagerAppWidgetProviderInfo.label,
                    description = appWidgetManagerAppWidgetProviderInfo.description,
                )
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        )
    }

    private suspend fun addEblanShortcutInfos(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanShortcutInfos =
            launcherAppsWrapper.getShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )?.map { launcherAppsShortcutInfo ->
                currentCoroutineContext().ensureActive()

                EblanShortcutInfo(
                    shortcutId = launcherAppsShortcutInfo.shortcutId,
                    serialNumber = launcherAppsShortcutInfo.serialNumber,
                    packageName = launcherAppsShortcutInfo.packageName,
                    shortLabel = launcherAppsShortcutInfo.shortLabel,
                    longLabel = launcherAppsShortcutInfo.longLabel,
                    icon = launcherAppsShortcutInfo.icon,
                    shortcutQueryFlag = launcherAppsShortcutInfo.shortcutQueryFlag,
                    isEnabled = launcherAppsShortcutInfo.isEnabled,
                    lastUpdateTime = launcherAppsShortcutInfo.lastUpdateTime,
                )
            }

        if (eblanShortcutInfos != null) {
            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfos,
            )
        }
    }

    private suspend fun addEblanShortcutConfigs(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanShortcutConfigs = launcherAppsWrapper.getShortcutConfigActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { shortcutConfigActivityInfo ->
            currentCoroutineContext().ensureActive()

            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

            val componentName =
                packageManagerWrapper.getComponentName(packageName = shortcutConfigActivityInfo.packageName)

            val applicationIcon = if (componentName != null) {
                val file = File(
                    directory,
                    componentName.hashCode().toString(),
                )

                file.absolutePath
            } else {
                val file = File(
                    directory,
                    shortcutConfigActivityInfo.packageName.hashCode().toString(),
                )

                packageManagerWrapper.getApplicationIcon(
                    packageName = shortcutConfigActivityInfo.packageName,
                    file = file,
                )
            }

            EblanShortcutConfig(
                componentName = shortcutConfigActivityInfo.componentName,
                packageName = shortcutConfigActivityInfo.packageName,
                serialNumber = shortcutConfigActivityInfo.serialNumber,
                activityIcon = shortcutConfigActivityInfo.activityIcon,
                activityLabel = shortcutConfigActivityInfo.activityLabel,
                applicationIcon = applicationIcon,
                applicationLabel = packageManagerWrapper.getApplicationLabel(
                    packageName = shortcutConfigActivityInfo.packageName,
                ),
            )
        }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = eblanShortcutConfigs,
        )
    }

    private suspend fun addIconPackInfos(
        iconPackInfoPackageName: String,
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
    ) {
        if (iconPackInfoPackageName.isEmpty()) return

        val iconPackInfoDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackInfoPackageName,
        ).apply { if (!exists()) mkdirs() }

        val appFilter = iconPackManager.getIconPackInfoComponents(packageName = iconPackInfoPackageName)

        launcherAppsActivityInfos.forEach { launcherAppsActivityInfo ->
            currentCoroutineContext().ensureActive()

            val file = File(
                iconPackInfoDirectory,
                launcherAppsActivityInfo.componentName.hashCode().toString(),
            )

            cacheIconPackFile(
                iconPackManager = iconPackManager,
                appFilter = appFilter,
                iconPackInfoPackageName = iconPackInfoPackageName,
                file = file,
                componentName = launcherAppsActivityInfo.componentName,
            )
        }
    }
}
