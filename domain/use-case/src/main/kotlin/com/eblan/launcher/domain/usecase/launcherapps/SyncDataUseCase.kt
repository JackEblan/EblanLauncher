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
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.FastAppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.model.UserData
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val iconPackManager: IconPackManager,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userData.first()

            launch {
                updateEblanLauncherAppsActivityInfos(userData = userData)
            }

            launch {
                updateAppWidgetManagerAppWidgetProviderInfos()
            }

            launch {
                updateEblanLauncherShortcutInfos()
            }

            launch {
                updateIconPackInfos(
                    iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                    iconPackManager = iconPackManager,
                    launcherAppsWrapper = launcherAppsWrapper,
                    fileManager = fileManager,
                )
            }
        }
    }

    private suspend fun updateEblanLauncherAppsActivityInfos(userData: UserData) {
        val oldFastEblanLauncherAppsActivityInfo =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()
                .map { eblanApplicationInfo ->
                    FastLauncherAppsActivityInfo(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        packageName = eblanApplicationInfo.packageName,
                        lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                    )
                }

        val newFastLauncherAppsActivityInfo = launcherAppsWrapper.getFastActivityList()

        if (oldFastEblanLauncherAppsActivityInfo == newFastLauncherAppsActivityInfo) return

        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

        updateEblanApplicationInfos(
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
        )

        insertApplicationInfoGridItems(
            launcherAppsActivityInfos = launcherAppsActivityInfos,
            experimentalSettings = userData.experimentalSettings,
            homeSettings = userData.homeSettings,
        )
    }

    private suspend fun updateEblanApplicationInfos(
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        iconPackInfoPackageName: String,
    ) {
        val oldSyncEblanApplicationInfos =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()
                .map { eblanApplicationInfo ->
                    SyncEblanApplicationInfo(
                        serialNumber = eblanApplicationInfo.serialNumber,
                        componentName = eblanApplicationInfo.componentName,
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                        lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                    )
                }

        val newEblanShortcutConfigs = mutableListOf<EblanShortcutConfig>()

        val newSyncEblanApplicationInfos = buildList {
            launcherAppsActivityInfos.forEach { launcherAppsActivityInfo ->
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
                        .map { shortcutConfigActivityInfo ->
                            currentCoroutineContext().ensureActive()

                            EblanShortcutConfig(
                                componentName = shortcutConfigActivityInfo.componentName,
                                packageName = shortcutConfigActivityInfo.packageName,
                                serialNumber = shortcutConfigActivityInfo.serialNumber,
                                activityIcon = shortcutConfigActivityInfo.activityIcon,
                                activityLabel = shortcutConfigActivityInfo.activityLabel,
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

        if (oldSyncEblanApplicationInfos != newSyncEblanApplicationInfos) {
            val syncEblanApplicationInfosToDelete =
                oldSyncEblanApplicationInfos - newSyncEblanApplicationInfos.toSet()

            eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
                syncEblanApplicationInfos = newSyncEblanApplicationInfos,
            )

            eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
                syncEblanApplicationInfos = syncEblanApplicationInfosToDelete,
            )

            syncEblanApplicationInfosToDelete.forEach { syncEblanApplicationInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueComponentName =
                    newSyncEblanApplicationInfos.none { newSyncEblanApplicationInfo ->
                        currentCoroutineContext().ensureActive()

                        newSyncEblanApplicationInfo.serialNumber != syncEblanApplicationInfoToDelete.serialNumber &&
                            newSyncEblanApplicationInfo.componentName == syncEblanApplicationInfoToDelete.componentName
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

        updateApplicationInfoGridItems(launcherAppsActivityInfos = launcherAppsActivityInfos)

        updateEblanShortcutConfigs(
            eblanShortcutConfigRepository = eblanShortcutConfigRepository,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }

    private suspend fun updateAppWidgetManagerAppWidgetProviderInfos() {
        val oldFastAppWidgetManagerAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()
                .map { eblanAppWidgetProviderInfo ->
                    FastAppWidgetManagerAppWidgetProviderInfo(
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                        packageName = eblanAppWidgetProviderInfo.packageName,
                        lastUpdateTime = eblanAppWidgetProviderInfo.lastUpdateTime,
                    )
                }

        val newFastAppWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getFastInstalledProviders()

        if (oldFastAppWidgetManagerAppWidgetProviderInfos == newFastAppWidgetManagerAppWidgetProviderInfos) return

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getInstalledProviders()

        updateEblanAppWidgetProviderInfos(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            eblanAppWidgetProviderInfoRepository = eblanAppWidgetProviderInfoRepository,
            widgetGridItemRepository = widgetGridItemRepository,
        )
    }

    private suspend fun updateEblanLauncherShortcutInfos() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldFastLauncherAppsShortcutInfos =
            eblanShortcutInfoRepository.eblanShortcutInfos.first().map { eblanShortcutInfo ->
                FastLauncherAppsShortcutInfo(
                    packageName = eblanShortcutInfo.packageName,
                    serialNumber = eblanShortcutInfo.serialNumber,
                    lastUpdateTime = eblanShortcutInfo.lastUpdateTime,
                )
            }

        val newFastLauncherAppsShortcutInfos = launcherAppsWrapper.getFastShortcuts()

        if (oldFastLauncherAppsShortcutInfos == newFastLauncherAppsShortcutInfos) return

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts() ?: return

        updateEblanShortcutInfos(launcherAppsShortcutInfos = launcherAppsShortcutInfos)
    }

    private suspend fun updateEblanShortcutInfos(launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>) {
        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos = launcherAppsShortcutInfos.map { launcherAppsShortcutInfo ->
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
                launcherAppsShortcutInfos = launcherAppsShortcutInfos,
                shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun insertApplicationInfoGridItems(
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        experimentalSettings: ExperimentalSettings,
        homeSettings: HomeSettings,
    ) {
        if (!experimentalSettings.firstLaunch) return

        @OptIn(ExperimentalUuidApi::class)
        suspend fun insertApplicationInfoGridItem(
            index: Int,
            launcherAppsActivityInfo: LauncherAppsActivityInfo,
            columns: Int,
            associate: Associate,
        ) {
            val startColumn = index % columns

            val startRow = index / columns

            applicationInfoGridItemRepository.insertApplicationInfoGridItem(
                applicationInfoGridItem = ApplicationInfoGridItem(
                    id = Uuid.random().toHexString(),
                    folderId = null,
                    page = 0,
                    startColumn = startColumn,
                    startRow = startRow,
                    columnSpan = 1,
                    rowSpan = 1,
                    associate = associate,
                    componentName = launcherAppsActivityInfo.componentName,
                    packageName = launcherAppsActivityInfo.packageName,
                    icon = launcherAppsActivityInfo.activityIcon,
                    label = launcherAppsActivityInfo.activityLabel,
                    override = false,
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    customIcon = null,
                    customLabel = null,
                    gridItemSettings = homeSettings.gridItemSettings,
                    doubleTap = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeUp = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeDown = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                ),
            )
        }

        launcherAppsActivityInfos.take(homeSettings.columns * homeSettings.rows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    columns = homeSettings.columns,
                    associate = Associate.Grid,
                )
            }

        launcherAppsActivityInfos.drop(homeSettings.columns * homeSettings.rows)
            .take(homeSettings.dockColumns * homeSettings.dockRows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    launcherAppsActivityInfo = launcherAppsActivityInfo,
                    columns = homeSettings.dockColumns,
                    associate = Associate.Dock,
                )
            }

        userDataRepository.updateExperimentalSettings(
            experimentalSettings = experimentalSettings.copy(
                firstLaunch = false,
            ),
        )
    }

    private suspend fun updateApplicationInfoGridItems(launcherAppsActivityInfos: List<LauncherAppsActivityInfo>) {
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

                    launcherAppsActivityInfo.componentName == applicationInfoGridItem.componentName && launcherAppsActivityInfo.serialNumber == applicationInfoGridItem.serialNumber
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
