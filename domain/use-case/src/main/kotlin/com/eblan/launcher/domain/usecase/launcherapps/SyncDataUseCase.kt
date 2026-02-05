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
import com.eblan.launcher.domain.model.DeleteEblanApplicationInfo
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.FastAppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
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
                updateEblanApplicationInfos(userData = userData)
            }

            launch {
                updateAppWidgetProviderInfos()
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

    private suspend fun updateEblanApplicationInfos(userData: UserData) {
        val oldFastEblanLauncherAppsActivityInfo =
            eblanApplicationInfoRepository.getEblanApplicationInfos().map { eblanApplicationInfo ->
                FastLauncherAppsActivityInfo(
                    serialNumber = eblanApplicationInfo.serialNumber,
                    packageName = eblanApplicationInfo.packageName,
                    lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                )
            }

        val newFastLauncherAppsActivityInfo = launcherAppsWrapper.getFastActivityList()

        if (oldFastEblanLauncherAppsActivityInfo == newFastLauncherAppsActivityInfo) return

        val newEblanShortcutConfigs = mutableListOf<EblanShortcutConfig>()

        val oldSyncEblanApplicationInfos =
            eblanApplicationInfoRepository.getEblanApplicationInfos().map { eblanApplicationInfo ->
                SyncEblanApplicationInfo(
                    serialNumber = eblanApplicationInfo.serialNumber,
                    componentName = eblanApplicationInfo.componentName,
                    packageName = eblanApplicationInfo.packageName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
                    lastUpdateTime = eblanApplicationInfo.lastUpdateTime,
                )
            }

        val newSyncEblanApplicationInfos = buildList {
            launcherAppsWrapper.getActivityList().forEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                updateIconPackInfoByComponentName(
                    componentName = launcherAppsActivityInfo.componentName,
                    iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                    fileManager = fileManager,
                    iconPackManager = iconPackManager,
                )

                newEblanShortcutConfigs.addAll(
                    launcherAppsWrapper.getShortcutConfigActivityList(
                        serialNumber = launcherAppsActivityInfo.serialNumber,
                        packageName = launcherAppsActivityInfo.packageName,
                    ).map { shortcutConfigActivityInfo ->
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
            val newDeleteEblanApplicationInfos =
                newSyncEblanApplicationInfos.map { syncEblanApplicationInfo ->
                    DeleteEblanApplicationInfo(
                        serialNumber = syncEblanApplicationInfo.serialNumber,
                        componentName = syncEblanApplicationInfo.componentName,
                        packageName = syncEblanApplicationInfo.packageName,
                        icon = syncEblanApplicationInfo.icon,
                    )
                }.toSet()

            val oldDeleteEblanApplicationInfos =
                oldSyncEblanApplicationInfos.map { syncEblanApplicationInfo ->
                    DeleteEblanApplicationInfo(
                        serialNumber = syncEblanApplicationInfo.serialNumber,
                        componentName = syncEblanApplicationInfo.componentName,
                        packageName = syncEblanApplicationInfo.packageName,
                        icon = syncEblanApplicationInfo.icon,
                    )
                }
                    .filter { deleteEblanApplicationInfo -> deleteEblanApplicationInfo !in newDeleteEblanApplicationInfos }

            eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
                syncEblanApplicationInfos = newSyncEblanApplicationInfos,
            )

            eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
                deleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
            )

            oldDeleteEblanApplicationInfos.forEach { oldDeleteEblanApplicationInfo ->
                currentCoroutineContext().ensureActive()

                val isUniqueComponentName =
                    eblanApplicationInfoRepository.getEblanApplicationInfos()
                        .none { eblanApplicationInfo ->
                            currentCoroutineContext().ensureActive()

                            eblanApplicationInfo.serialNumber != oldDeleteEblanApplicationInfo.serialNumber && eblanApplicationInfo.componentName == oldDeleteEblanApplicationInfo.componentName
                        }

                if (isUniqueComponentName) {
                    oldDeleteEblanApplicationInfo.icon?.let { icon ->
                        val iconFile = File(icon)

                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }

                    val iconPacksDirectory = File(
                        fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                        userData.generalSettings.iconPackInfoPackageName,
                    )

                    val iconPackFile = File(
                        iconPacksDirectory,
                        oldDeleteEblanApplicationInfo.componentName.hashCode().toString(),
                    )

                    if (iconPackFile.exists()) {
                        iconPackFile.delete()
                    }
                }
            }

            updateEblanShortcutConfigs(newEblanShortcutConfigs = newEblanShortcutConfigs)

            updateApplicationInfoGridItems(
                eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
                applicationInfoGridItemRepository = applicationInfoGridItemRepository,
            )
        }

        insertApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            experimentalSettings = userData.experimentalSettings,
            homeSettings = userData.homeSettings,
        )
    }

    private suspend fun updateAppWidgetProviderInfos() {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val oldFastAppWidgetManagerAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
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

        val appWidgetManagerAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.map { appWidgetManagerAppWidgetProviderInfo ->
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
                    applicationIcon = icon,
                    applicationLabel = packageManagerWrapper.getApplicationLabel(
                        packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                    ).toString(),
                    lastUpdateTime = appWidgetManagerAppWidgetProviderInfo.lastUpdateTime,
                    label = appWidgetManagerAppWidgetProviderInfo.label,
                )
            }

        if (oldEblanAppWidgetProviderInfos != newEblanAppWidgetProviderInfos) {
            val eblanAppWidgetProviderInfosToDelete =
                oldEblanAppWidgetProviderInfos - newEblanAppWidgetProviderInfos.toSet()

            eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfos,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfosToDelete,
            )

            eblanAppWidgetProviderInfosToDelete.forEach { eblanAppWidgetProviderInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniquePackageName = eblanApplicationInfoRepository.getEblanApplicationInfos()
                    .none { eblanApplicationInfo ->
                        currentCoroutineContext().ensureActive()

                        eblanApplicationInfo.packageName == eblanAppWidgetProviderInfoToDelete.packageName
                    }

                if (isUniquePackageName) {
                    eblanAppWidgetProviderInfoToDelete.applicationIcon?.let { icon ->
                        val iconFile = File(icon)

                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
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
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
                widgetGridItemRepository = widgetGridItemRepository,
            )
        }
    }

    private suspend fun updateEblanLauncherShortcutInfos() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldFastLauncherAppsShortcutInfos =
            eblanShortcutInfoRepository.getEblanShortcutInfos().map { eblanShortcutInfo ->
                FastLauncherAppsShortcutInfo(
                    packageName = eblanShortcutInfo.packageName,
                    serialNumber = eblanShortcutInfo.serialNumber,
                    lastUpdateTime = eblanShortcutInfo.lastUpdateTime,
                )
            }

        val newFastLauncherAppsShortcutInfos = launcherAppsWrapper.getFastShortcuts()

        if (oldFastLauncherAppsShortcutInfos == newFastLauncherAppsShortcutInfos) return

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts() ?: return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos()

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
                    eblanShortcutInfoRepository.getEblanShortcutInfos().none { eblanShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        eblanShortcutInfo.serialNumber != eblanShortcutInfoToDelete.serialNumber && eblanShortcutInfo.shortcutId == eblanShortcutInfoToDelete.shortcutId
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
                eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(),
                shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }

    private suspend fun updateEblanShortcutConfigs(
        newEblanShortcutConfigs: List<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs()

        if (oldEblanShortcutConfigs != newEblanShortcutConfigs) {
            val eblanShortcutConfigsToDelete =
                oldEblanShortcutConfigs - newEblanShortcutConfigs.toSet()

            eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
                eblanShortcutConfigs = newEblanShortcutConfigs,
            )

            eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
                eblanShortcutConfigs = eblanShortcutConfigsToDelete,
            )

            eblanShortcutConfigsToDelete.forEach { eblanShortcutConfigToDelete ->
                currentCoroutineContext().ensureActive()

                val isUniqueComponentName = eblanShortcutConfigRepository.getEblanShortcutConfigs()
                    .none { eblanShortcutConfig ->
                        currentCoroutineContext().ensureActive()

                        eblanShortcutConfig.serialNumber != eblanShortcutConfigToDelete.serialNumber && eblanShortcutConfig.componentName == eblanShortcutConfigToDelete.componentName
                    }

                if (isUniqueComponentName) {
                    eblanShortcutConfigToDelete.activityIcon?.let { activityIcon ->
                        val activityIconFile = File(activityIcon)

                        if (activityIconFile.exists()) {
                            activityIconFile.delete()
                        }
                    }
                }
            }

            updateShortcutConfigGridItems(
                eblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs(),
                shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun insertApplicationInfoGridItems(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        experimentalSettings: ExperimentalSettings,
        homeSettings: HomeSettings,
    ) {
        if (!experimentalSettings.firstLaunch) return

        @OptIn(ExperimentalUuidApi::class)
        suspend fun insertApplicationInfoGridItem(
            index: Int,
            eblanApplicationInfo: EblanApplicationInfo,
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
                    componentName = eblanApplicationInfo.componentName,
                    packageName = eblanApplicationInfo.packageName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
                    override = false,
                    serialNumber = eblanApplicationInfo.serialNumber,
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

        eblanApplicationInfos.take(homeSettings.columns * homeSettings.rows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
                    columns = homeSettings.columns,
                    associate = Associate.Grid,
                )
            }

        eblanApplicationInfos.drop(homeSettings.columns * homeSettings.rows)
            .take(homeSettings.dockColumns * homeSettings.dockRows)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
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
}
