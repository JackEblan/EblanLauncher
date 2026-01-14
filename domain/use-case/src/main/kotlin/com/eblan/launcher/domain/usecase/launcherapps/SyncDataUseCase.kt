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
import com.eblan.launcher.domain.framework.NotificationManagerWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.FastAppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.FastLauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.model.UpdateShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateWidgetGridItem
import com.eblan.launcher.domain.model.UserData
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.iconpack.cacheIconPackFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val notificationManagerWrapper: NotificationManagerWrapper,
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
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            notificationManagerWrapper.notifySyncData(contentText = "This may take a while")

            val userData = userDataRepository.userData.first()

            measureTimeMillis {
                joinAll(
                    launch {
                        updateEblanLauncherAppsActivityInfos(userData = userData)
                    },
                    launch {
                        updateAppWidgetManagerAppWidgetProviderInfos()
                    },
                    launch {
                        updateEblanLauncherShortcutInfos()
                    },
                    launch {
                        updateIconPackInfos(
                            iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                        )
                    },
                )
            }.also { ms ->
                notificationManagerWrapper.notifySyncData(contentText = "Syncing data took $ms ms")
            }
        }
    }

    private suspend fun updateEblanLauncherShortcutInfos() {
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

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts()

        updateEblanShortcutInfos(launcherAppsShortcutInfos = launcherAppsShortcutInfos)

        updateShortcutInfoGridItems(launcherAppsShortcutInfos = launcherAppsShortcutInfos)
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

        updateApplicationInfoGridItems(launcherAppsActivityInfos = launcherAppsActivityInfos)
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

        updateEblanAppWidgetProviderInfos(appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos)

        updateWidgetGridItems(appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos)
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

        val newSyncEblanApplicationInfos =
            launcherAppsActivityInfos.map { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                newEblanShortcutConfigs.addAll(
                    launcherAppsWrapper.getShortcutConfigActivityList(
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

                SyncEblanApplicationInfo(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    componentName = launcherAppsActivityInfo.componentName,
                    packageName = launcherAppsActivityInfo.packageName,
                    icon = launcherAppsActivityInfo.activityIcon,
                    label = launcherAppsActivityInfo.activityLabel,
                    lastUpdateTime = launcherAppsActivityInfo.lastUpdateTime,
                )
            }

        if (oldSyncEblanApplicationInfos != newSyncEblanApplicationInfos) {
            val upsertEblanApplicationInfosToDelete =
                oldSyncEblanApplicationInfos - newSyncEblanApplicationInfos.toSet()

            eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
                syncEblanApplicationInfos = newSyncEblanApplicationInfos,
            )

            eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
                syncEblanApplicationInfos = upsertEblanApplicationInfosToDelete,
            )

            upsertEblanApplicationInfosToDelete.forEach { eblanApplicationInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUnique = newSyncEblanApplicationInfos.none { newEblanApplicationInfo ->
                    newEblanApplicationInfo.packageName == eblanApplicationInfoToDelete.packageName && newEblanApplicationInfo.serialNumber != eblanApplicationInfoToDelete.serialNumber
                }

                if (isUnique) {
                    eblanApplicationInfoToDelete.icon?.let { icon ->
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
                        eblanApplicationInfoToDelete.packageName,
                    )

                    if (iconPackFile.exists()) {
                        iconPackFile.delete()
                    }
                }
            }
        }

        updateEblanShortcutConfigs(
            eblanShortcutConfigRepository = eblanShortcutConfigRepository,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
        )
    }

    private suspend fun updateEblanShortcutConfigs(
        eblanShortcutConfigRepository: EblanShortcutConfigRepository,
        newEblanShortcutConfigs: List<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigs = eblanShortcutConfigRepository.eblanShortcutConfigs.first()

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

                val isUnique = newEblanShortcutConfigs.none { newEblanShortcutConfig ->
                    newEblanShortcutConfig.packageName == eblanShortcutConfigToDelete.packageName && newEblanShortcutConfig.serialNumber != eblanShortcutConfigToDelete.serialNumber
                }

                if (isUnique) {
                    eblanShortcutConfigToDelete.activityIcon?.let { activityIcon ->
                        val activityIconFile = File(activityIcon)

                        if (activityIconFile.exists()) {
                            activityIconFile.delete()
                        }

                        eblanShortcutConfigToDelete.applicationIcon?.let { applicationIcon ->
                            val applicationIconFile = File(applicationIcon)

                            if (applicationIconFile.exists()) {
                                applicationIconFile.delete()
                            }
                        }
                    }
                }
            }
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

    private suspend fun updateIconPackInfos(iconPackInfoPackageName: String) {
        if (iconPackInfoPackageName.isNotEmpty()) {
            val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

            val appFilter = iconPackManager.parseAppFilter(packageName = iconPackInfoPackageName)

            val iconPackDirectory = File(
                fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
                iconPackInfoPackageName,
            ).apply { if (!exists()) mkdirs() }

            val installedPackageNames =
                launcherAppsActivityInfos.onEach { launcherAppsActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    cacheIconPackFile(
                        iconPackManager = iconPackManager,
                        appFilter = appFilter,
                        iconPackInfoPackageName = iconPackInfoPackageName,
                        iconPackInfoDirectory = iconPackDirectory,
                        componentName = launcherAppsActivityInfo.componentName,
                        packageName = launcherAppsActivityInfo.packageName,
                    )
                }.map { launcherAppsActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    launcherAppsActivityInfo.componentName.replace(
                        "/",
                        "-",
                    )
                }

            iconPackDirectory.listFiles()?.filter { it.isFile && it.name !in installedPackageNames }
                ?.forEach {
                    currentCoroutineContext().ensureActive()

                    it.delete()
                }
        }
    }

    private suspend fun updateEblanAppWidgetProviderInfos(appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.map { appWidgetManagerAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val componentName =
                    packageManagerWrapper.getComponentName(packageName = appWidgetManagerAppWidgetProviderInfo.packageName)

                val icon = if (componentName != null) {
                    val file = File(
                        directory,
                        componentName.replace("/", "-"),
                    )

                    file.absolutePath
                } else {
                    val file = File(
                        directory,
                        appWidgetManagerAppWidgetProviderInfo.packageName,
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
        }
    }

    private suspend fun updateEblanShortcutInfos(launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos = launcherAppsShortcutInfos?.map { launcherAppsShortcutInfo ->
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

        if (newEblanShortcutInfos != null && oldEblanShortcutInfos != newEblanShortcutInfos) {
            val eblanShortcutInfosToDelete = oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfos,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfosToDelete,
            )

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfoToDelete ->
                currentCoroutineContext().ensureActive()

                eblanShortcutInfoToDelete.icon?.let { icon ->
                    val iconFile = File(icon)

                    if (iconFile.exists()) {
                        iconFile.delete()
                    }
                }
            }
        }
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

                    launcherAppsActivityInfo.packageName == applicationInfoGridItem.packageName && launcherAppsActivityInfo.serialNumber == applicationInfoGridItem.serialNumber
                }

            if (launcherAppsActivityInfo != null) {
                updateApplicationInfoGridItems.add(
                    UpdateApplicationInfoGridItem(
                        id = applicationInfoGridItem.id,
                        componentName = launcherAppsActivityInfo.componentName,
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

    private suspend fun updateWidgetGridItems(appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val updateWidgetGridItems = mutableListOf<UpdateWidgetGridItem>()

        val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

        val widgetGridItems = widgetGridItemRepository.widgetGridItems.first()

        widgetGridItems.filterNot { widgetGridItem ->
            widgetGridItem.override
        }.forEach { widgetGridItem ->
            currentCoroutineContext().ensureActive()

            val appWidgetManagerAppWidgetProviderInfo =
                appWidgetManagerAppWidgetProviderInfos.find { appWidgetManagerAppWidgetProviderInfo ->
                    currentCoroutineContext().ensureActive()

                    appWidgetManagerAppWidgetProviderInfo.componentName == widgetGridItem.componentName
                }

            if (appWidgetManagerAppWidgetProviderInfo != null) {
                updateWidgetGridItems.add(
                    UpdateWidgetGridItem(
                        id = widgetGridItem.id,
                        componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
                        configure = appWidgetManagerAppWidgetProviderInfo.configure,
                        minWidth = appWidgetManagerAppWidgetProviderInfo.minWidth,
                        minHeight = appWidgetManagerAppWidgetProviderInfo.minHeight,
                        resizeMode = appWidgetManagerAppWidgetProviderInfo.resizeMode,
                        minResizeWidth = appWidgetManagerAppWidgetProviderInfo.minResizeWidth,
                        minResizeHeight = appWidgetManagerAppWidgetProviderInfo.minResizeHeight,
                        maxResizeWidth = appWidgetManagerAppWidgetProviderInfo.maxResizeWidth,
                        maxResizeHeight = appWidgetManagerAppWidgetProviderInfo.maxResizeHeight,
                        targetCellHeight = appWidgetManagerAppWidgetProviderInfo.targetCellWidth,
                        targetCellWidth = appWidgetManagerAppWidgetProviderInfo.targetCellHeight,
                    ),
                )
            } else {
                deleteWidgetGridItems.add(widgetGridItem)
            }
        }

        widgetGridItemRepository.updateWidgetGridItems(updateWidgetGridItems = updateWidgetGridItems)

        widgetGridItemRepository.deleteWidgetGridItemsByPackageName(widgetGridItems = deleteWidgetGridItems)
    }

    suspend fun updateShortcutInfoGridItems(launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val updateShortcutInfoGridItems = mutableListOf<UpdateShortcutInfoGridItem>()

        val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutInfoGridItems = shortcutInfoGridItemRepository.shortcutInfoGridItems.first()

        if (launcherAppsShortcutInfos != null) {
            shortcutInfoGridItems.filterNot { shortcutInfoGridItem ->
                shortcutInfoGridItem.override
            }.forEach { shortcutInfoGridItem ->
                currentCoroutineContext().ensureActive()

                val launcherAppsShortcutInfo =
                    launcherAppsShortcutInfos.find { launcherAppsShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        launcherAppsShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId && launcherAppsShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber
                    }

                if (launcherAppsShortcutInfo != null) {
                    updateShortcutInfoGridItems.add(
                        UpdateShortcutInfoGridItem(
                            id = shortcutInfoGridItem.id,
                            shortLabel = launcherAppsShortcutInfo.shortLabel,
                            longLabel = launcherAppsShortcutInfo.longLabel,
                            isEnabled = launcherAppsShortcutInfo.isEnabled,
                            icon = launcherAppsShortcutInfo.icon,
                        ),
                    )
                } else {
                    deleteShortcutInfoGridItems.add(shortcutInfoGridItem)
                }
            }

            shortcutInfoGridItemRepository.updateShortcutInfoGridItems(
                updateShortcutInfoGridItems = updateShortcutInfoGridItems,
            )

            shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = deleteShortcutInfoGridItems)
        }
    }
}
