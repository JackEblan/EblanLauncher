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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AddPackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            launcherAppsWrapper.getActivityList(
                serialNumber = serialNumber,
                packageName = packageName,
            ).forEach { launcherAppsActivityInfo ->
                val icon = launcherAppsActivityInfo.applicationIcon
                    ?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                            name = packageName,
                            byteArray = byteArray,
                        )
                    }

                addEblanApplicationInfo(
                    serialNumber = serialNumber,
                    packageName = packageName,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    icon = icon,
                )

                addEblanAppWidgetProviderInfos(
                    serialNumber = serialNumber,
                    packageName = packageName,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    icon = icon,
                )

                updateIconPackInfoByPackageNameUseCase(
                    packageName = launcherAppsActivityInfo.packageName,
                    componentName = launcherAppsActivityInfo.componentName,
                )
            }

            addEblanShortcutInfos()

            addEblanShortcutConfigs(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun addEblanApplicationInfo(
        serialNumber: Long,
        packageName: String,
        launcherAppsActivityInfo: LauncherAppsActivityInfo,
        icon: String?,
    ) {
        val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

        if (componentName != null) {
            eblanApplicationInfoRepository.upsertEblanApplicationInfo(
                eblanApplicationInfo = EblanApplicationInfo(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    packageName = packageName,
                    icon = icon,
                    label = launcherAppsActivityInfo.applicationLabel,
                    customIcon = null,
                    customLabel = null,
                ),
            )
        }
    }

    private suspend fun addEblanAppWidgetProviderInfos(
        serialNumber: Long,
        packageName: String,
        launcherAppsActivityInfo: LauncherAppsActivityInfo,
        icon: String?,
    ) {
        val eblanAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }.map { appWidgetManagerAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                val preview =
                    appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                            name = appWidgetManagerAppWidgetProviderInfo.componentName.replace(
                                "/",
                                "-",
                            ),
                            byteArray = currentPreview,
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
                    preview = preview,
                    label = launcherAppsActivityInfo.applicationLabel,
                    icon = icon,
                )
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        )
    }

    private suspend fun addEblanShortcutInfos() {
        val eblanShortcutInfos =
            launcherAppsWrapper.getShortcuts()?.map { launcherAppsShortcutInfo ->
                currentCoroutineContext().ensureActive()

                val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                    fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                        name = launcherAppsShortcutInfo.shortcutId,
                        byteArray = byteArray,
                    )
                }

                EblanShortcutInfo(
                    shortcutId = launcherAppsShortcutInfo.shortcutId,
                    serialNumber = launcherAppsShortcutInfo.serialNumber,
                    packageName = launcherAppsShortcutInfo.packageName,
                    shortLabel = launcherAppsShortcutInfo.shortLabel,
                    longLabel = launcherAppsShortcutInfo.longLabel,
                    icon = icon,
                    shortcutQueryFlag = launcherAppsShortcutInfo.shortcutQueryFlag,
                    isEnabled = launcherAppsShortcutInfo.isEnabled,
                )
            }

        if (eblanShortcutInfos != null) {
            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfos,
            )
        }
    }

    private suspend fun addEblanShortcutConfigs(serialNumber: Long, packageName: String) {
        val eblanShortcutConfigs = launcherAppsWrapper.getShortcutConfigActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { launcherAppsActivityInfo ->
            val activityIcon = launcherAppsActivityInfo.activityIcon?.let { byteArray ->
                fileManager.updateAndGetFilePath(
                    directory = fileManager.getFilesDirectory(FileManager.SHORTCUT_CONFIGS_DIR),
                    name = launcherAppsActivityInfo.componentName.replace("/", "-"),
                    byteArray = byteArray,
                )
            }

            val applicationIcon =
                launcherAppsActivityInfo.applicationIcon?.let { byteArray ->
                    fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        name = launcherAppsActivityInfo.packageName,
                        byteArray = byteArray,
                    )
                }

            val applicationLabel =
                packageManagerWrapper.getApplicationLabel(packageName = launcherAppsActivityInfo.packageName)

            EblanShortcutConfig(
                componentName = launcherAppsActivityInfo.componentName,
                packageName = launcherAppsActivityInfo.packageName,
                serialNumber = launcherAppsActivityInfo.serialNumber,
                activityIcon = activityIcon,
                activityLabel = launcherAppsActivityInfo.activityLabel,
                applicationIcon = applicationIcon,
                applicationLabel = applicationLabel,
            )
        }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = eblanShortcutConfigs,
        )
    }
}
