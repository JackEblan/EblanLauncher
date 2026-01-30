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
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
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

        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

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
            launcherAppsWrapper.getActivityList(
                serialNumber = serialNumber,
                packageName = packageName,
            ).forEach { launcherAppsActivityInfo ->
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

        if (oldSyncEblanApplicationInfosByPackageName !=
            newSyncEblanApplicationInfosByPackageName
        ) {
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
                    launcherAppsActivityInfos.none { launcherAppsActivityInfo ->
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
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        updateEblanShortcutConfigs(
            eblanShortcutConfigRepository = eblanShortcutConfigRepository,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }

    private suspend fun updateEblanAppWidgetProviderInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()
            .filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.serialNumber == serialNumber && appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }

        updateEblanAppWidgetProviderInfos(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            eblanAppWidgetProviderInfoRepository = eblanAppWidgetProviderInfoRepository,
            widgetGridItemRepository = widgetGridItemRepository,
        )
    }

    private suspend fun updateEblanShortcutInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts() ?: return

        val launcherAppsShortcutInfosByPackageName =
            launcherAppsWrapper.getShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            ) ?: return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos =
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

        if (oldEblanShortcutInfos != newEblanShortcutInfos) {
            val eblanShortcutInfosToDelete = oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfos,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfosToDelete,
            )

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueShortcutId =
                    launcherAppsShortcutInfos.none { launcherAppsShortcutInfo ->
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
                launcherAppsShortcutInfos = launcherAppsShortcutInfosByPackageName,
                shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }

    private suspend fun updateApplicationInfoGridItems(
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    ) {
        val updateApplicationInfoGridItems = mutableListOf<UpdateApplicationInfoGridItem>()

        val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val applicationInfoGridItems =
            applicationInfoGridItemRepository.applicationInfoGridItems.first()

        applicationInfoGridItems.filterNot { applicationInfoGridItem ->
            applicationInfoGridItem.override
        }.forEach { applicationInfoGridItem ->
            currentCoroutineContext().ensureActive()

            val launcherAppsActivityInfo =
                launcherAppsActivityInfos.find { launcherAppsActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    launcherAppsActivityInfo.serialNumber == applicationInfoGridItem.serialNumber &&
                        launcherAppsActivityInfo.componentName == applicationInfoGridItem.componentName
                }

            if (launcherAppsActivityInfo != null) {
                updateApplicationInfoGridItems.add(
                    UpdateApplicationInfoGridItem(
                        id = applicationInfoGridItem.id,
                        componentName = launcherAppsActivityInfo.componentName,
                        icon = launcherAppsActivityInfo.activityIcon,
                        label = launcherAppsActivityInfo.activityLabel,
                    ),
                )
            } else {
                deleteApplicationInfoGridItems.add(applicationInfoGridItem)
            }
        }

        applicationInfoGridItemRepository.updateApplicationInfoGridItems(
            updateApplicationInfoGridItems = updateApplicationInfoGridItems,
        )

        applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
            applicationInfoGridItems = deleteApplicationInfoGridItems,
        )
    }
}
