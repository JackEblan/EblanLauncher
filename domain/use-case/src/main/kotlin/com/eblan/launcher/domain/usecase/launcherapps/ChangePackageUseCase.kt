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
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.iconpack.updateIconPackInfoByComponentName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userData.first()

            if (!userData.experimentalSettings.syncData) return@withContext

            updateEblanApplicationInfo(
                packageName = packageName,
                serialNumber = serialNumber,
                iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
            )

            updateEblanAppWidgetProviderInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateEblanShortcutInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun updateEblanApplicationInfo(
        packageName: String,
        serialNumber: Long,
        iconPackInfoPackageName: String,
    ) {
        val newEblanShortcutConfigs = mutableListOf<EblanShortcutConfig>()

        val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        val oldSyncEblanApplicationInfosByPackageName =
            eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            ).map { eblanApplicationInfo ->
                SyncEblanApplicationInfo(
                    serialNumber = eblanApplicationInfo.serialNumber,
                    componentName = eblanApplicationInfo.componentName,
                    packageName = eblanApplicationInfo.packageName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
                    lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                )
            }

        val newSyncEblanApplicationInfosByPackageName = buildList {
            launcherAppsActivityInfosByPackageName.forEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                updateIconPackInfoByComponentName(
                    componentName = launcherAppsActivityInfo.componentName,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    fileManager = fileManager,
                    iconPackManager = iconPackManager,
                )

                newEblanShortcutConfigs.addAll(
                    launcherAppsWrapper
                        .getShortcutConfigActivityList(
                            serialNumber = launcherAppsActivityInfo.serialNumber,
                            packageName = launcherAppsActivityInfo.packageName,
                        )
                        .map { shortcutConfigActivity ->
                            currentCoroutineContext().ensureActive()

                            EblanShortcutConfig(
                                componentName = shortcutConfigActivity.componentName,
                                packageName = shortcutConfigActivity.packageName,
                                serialNumber = shortcutConfigActivity.serialNumber,
                                activityIcon = shortcutConfigActivity.activityIcon,
                                activityLabel = shortcutConfigActivity.activityLabel,
                                applicationIcon = launcherAppsActivityInfo.activityIcon,
                                applicationLabel = launcherAppsActivityInfo.activityLabel,
                                lastUpdateTime = launcherAppsActivityInfo.lastUpdateTime,
                            )
                        },
                )

                add(
                    SyncEblanApplicationInfo(
                        serialNumber = launcherAppsActivityInfo.serialNumber,
                        componentName = launcherAppsActivityInfo.componentName,
                        packageName = launcherAppsActivityInfo.packageName,
                        icon = launcherAppsActivityInfo.activityIcon,
                        label = launcherAppsActivityInfo.activityLabel,
                        lastUpdateTime = launcherAppsActivityInfo.lastUpdateTime,
                    ),
                )
            }
        }

        if (oldSyncEblanApplicationInfosByPackageName != newSyncEblanApplicationInfosByPackageName) {
            val syncEblanApplicationInfosToDelete =
                oldSyncEblanApplicationInfosByPackageName - newSyncEblanApplicationInfosByPackageName.toSet()

            eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
                syncEblanApplicationInfos = newSyncEblanApplicationInfosByPackageName,
            )

            eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
                syncEblanApplicationInfos = syncEblanApplicationInfosToDelete,
            )

            syncEblanApplicationInfosToDelete.forEach { syncEblanApplicationInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueComponentName =
                    eblanApplicationInfoRepository.eblanApplicationInfos.first()
                        .none { launcherAppsActivityInfo ->
                            currentCoroutineContext().ensureActive()

                            launcherAppsActivityInfo.serialNumber != syncEblanApplicationInfoToDelete.serialNumber &&
                                    launcherAppsActivityInfo.componentName == syncEblanApplicationInfoToDelete.componentName
                        }

                if (isUniqueComponentName) {
                    syncEblanApplicationInfoToDelete.icon?.let { icon ->
                        val iconFile = File(icon)

                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }

                    val iconPacksDirectory = File(
                        fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                        iconPackInfoPackageName,
                    )

                    val iconPackFile = File(
                        iconPacksDirectory,
                        syncEblanApplicationInfoToDelete.componentName.hashCode().toString(),
                    )

                    if (iconPackFile.exists()) {
                        iconPackFile.delete()
                    }
                }
            }
        }

        updateApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.eblanApplicationInfos.first(),
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        updateEblanShortcutConfigs(
            serialNumber = serialNumber,
            packageName = packageName,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
        )
    }

    private suspend fun updateEblanAppWidgetProviderInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfosByPackageName =
            appWidgetManagerWrapper.getInstalledProviders()
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.serialNumber == serialNumber &&
                            appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }

        val oldEblanAppWidgetProviderInfosByPackageName =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.serialNumber == serialNumber &&
                            appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }

        val newEblanAppWidgetProviderInfosByPackageName =
            appWidgetManagerAppWidgetProviderInfosByPackageName
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
                        serialNumber = appWidgetManagerAppWidgetProviderInfo.serialNumber,
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
                        icon = icon,
                        label = packageManagerWrapper.getApplicationLabel(
                            packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                        ).toString(),
                        lastUpdateTime = appWidgetManagerAppWidgetProviderInfo.lastUpdateTime,
                    )
                }

        if (oldEblanAppWidgetProviderInfosByPackageName != newEblanAppWidgetProviderInfosByPackageName) {
            val eblanAppWidgetProviderInfosToDelete =
                oldEblanAppWidgetProviderInfosByPackageName - newEblanAppWidgetProviderInfosByPackageName.toSet()

            eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfosByPackageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosToDelete,
            )

            eblanAppWidgetProviderInfosToDelete.forEach { eblanAppWidgetProviderInfoToDelete ->
                currentCoroutineContext().ensureActive()

                eblanAppWidgetProviderInfoToDelete.icon?.let { icon ->
                    val iconFile = File(icon)

                    if (iconFile.exists()) {
                        iconFile.delete()
                    }
                }

                eblanAppWidgetProviderInfoToDelete.preview?.let { preview ->
                    val previewFile = File(preview)

                    if (previewFile.exists()) {
                        previewFile.delete()
                    }
                }
            }

            updateWidgetGridItems(
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first(),
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
                widgetGridItemRepository = widgetGridItemRepository,
            )
        }
    }

    private suspend fun updateEblanShortcutInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfosByPackageName =
            launcherAppsWrapper.getShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            ) ?: return

        val oldEblanShortcutInfosByPackageName =
            eblanShortcutInfoRepository.eblanShortcutInfos.first().filter { eblanShortcutInfo ->
                eblanShortcutInfo.serialNumber == serialNumber &&
                        eblanShortcutInfo.packageName == packageName
            }

        val newEblanShortcutInfosByPackageName =
            launcherAppsShortcutInfosByPackageName.map { launcherAppsShortcutInfo ->
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

        if (oldEblanShortcutInfosByPackageName != newEblanShortcutInfosByPackageName) {
            val eblanShortcutInfosToDelete =
                oldEblanShortcutInfosByPackageName - newEblanShortcutInfosByPackageName.toSet()

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfosByPackageName,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfosToDelete,
            )

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueShortcutId =
                    eblanShortcutInfoRepository.eblanShortcutInfos.first()
                        .none { launcherAppsShortcutInfo ->
                            currentCoroutineContext().ensureActive()

                            launcherAppsShortcutInfo.serialNumber != eblanShortcutInfoToDelete.serialNumber &&
                                    launcherAppsShortcutInfo.shortcutId == eblanShortcutInfoToDelete.shortcutId
                        }

                if (isUniqueShortcutId) {
                    eblanShortcutInfoToDelete.icon?.let { icon ->
                        val iconFile = File(icon)

                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }
                }
            }

            updateShortcutInfoGridItems(
                eblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first(),
                shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }

    private suspend fun updateEblanShortcutConfigs(
        serialNumber: Long,
        packageName: String,
        newEblanShortcutConfigs: List<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigsByPackageName =
            eblanShortcutConfigRepository.eblanShortcutConfigs.first()
                .filter { eblanShortcutConfig ->
                    eblanShortcutConfig.serialNumber == serialNumber &&
                            eblanShortcutConfig.packageName == packageName
                }

        if (oldEblanShortcutConfigsByPackageName != newEblanShortcutConfigs) {
            val eblanShortcutConfigsToDelete =
                oldEblanShortcutConfigsByPackageName - newEblanShortcutConfigs.toSet()

            eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
                eblanShortcutConfigs = newEblanShortcutConfigs,
            )

            eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
                eblanShortcutConfigs = eblanShortcutConfigsToDelete,
            )

            val eblanShortcutConfigs = eblanShortcutConfigRepository.eblanShortcutConfigs.first()

            eblanShortcutConfigsToDelete.forEach { eblanShortcutConfigToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueComponentName =
                    eblanShortcutConfigs.none { newEblanShortcutConfig ->
                        currentCoroutineContext().ensureActive()

                        newEblanShortcutConfig.serialNumber != eblanShortcutConfigToDelete.serialNumber &&
                                newEblanShortcutConfig.componentName == eblanShortcutConfigToDelete.componentName
                    }

                val isUniquePackageName =
                    eblanShortcutConfigs.none { newEblanShortcutConfig ->
                        currentCoroutineContext().ensureActive()

                        newEblanShortcutConfig.serialNumber != eblanShortcutConfigToDelete.serialNumber &&
                                newEblanShortcutConfig.packageName == eblanShortcutConfigToDelete.packageName
                    }

                if (isUniqueComponentName) {
                    eblanShortcutConfigToDelete.activityIcon?.let { activityIcon ->
                        val activityIconFile = File(activityIcon)

                        if (activityIconFile.exists()) {
                            activityIconFile.delete()
                        }
                    }
                }

                if (isUniquePackageName) {
                    eblanShortcutConfigToDelete.applicationIcon?.let { applicationIcon ->
                        val applicationIconFile = File(applicationIcon)

                        if (applicationIconFile.exists()) {
                            applicationIconFile.delete()
                        }
                    }
                }
            }

            updateShortcutConfigGridItems(
                eblanShortcutConfigs = eblanShortcutConfigRepository.eblanShortcutConfigs.first(),
                shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }
}
