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
import com.eblan.launcher.domain.framework.NotificationManagerWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.model.UpdateShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateWidgetGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val notificationManagerWrapper: NotificationManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val updateIconPackInfosUseCase: UpdateIconPackInfosUseCase,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val updateEblanShortcutConfigActivitiesUseCase: UpdateEblanShortcutConfigActivitiesUseCase,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            try {
                notificationManagerWrapper.notifySyncData()

                joinAll(
                    launch {
                        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

                        updateEblanApplicationInfos(launcherAppsActivityInfos = launcherAppsActivityInfos)

                        updateApplicationInfoGridItems(launcherAppsActivityInfos = launcherAppsActivityInfos)
                    },
                    launch {
                        val appWidgetManagerAppWidgetProviderInfos =
                            appWidgetManagerWrapper.getInstalledProviders()

                        updateEblanAppWidgetProviderInfos(appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos)

                        updateWidgetGridItems(appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos)
                    },
                    launch {
                        updateEblanShortcutInfos(launcherAppsShortcutInfos = launcherAppsWrapper.getShortcuts())
                    },
                    launch {
                        updateShortcutInfoGridItems(pinnedLauncherAppsShortcutInfos = launcherAppsWrapper.getPinnedShortcuts())
                    },
                )
            } finally {
                notificationManagerWrapper.cancelSyncData()
            }
        }
    }

    private suspend fun updateEblanApplicationInfos(launcherAppsActivityInfos: List<LauncherAppsActivityInfo>) {
        val iconPackInfoPackageName =
            userDataRepository.userData.first().generalSettings.iconPackInfoPackageName

        val oldEblanApplicationInfos =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val newEblanApplicationInfos =
            launcherAppsActivityInfos.onEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                updateEblanShortcutConfigActivitiesUseCase(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    packageName = launcherAppsActivityInfo.packageName,
                )
            }.map { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                val icon = launcherAppsActivityInfo.icon?.let { byteArray ->
                    fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        name = launcherAppsActivityInfo.packageName,
                        byteArray = byteArray,
                    )
                }

                EblanApplicationInfo(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    componentName = launcherAppsActivityInfo.componentName,
                    packageName = launcherAppsActivityInfo.packageName,
                    icon = icon,
                    label = launcherAppsActivityInfo.label,
                )
            }

        if (oldEblanApplicationInfos != newEblanApplicationInfos) {
            val eblanApplicationInfosToDelete =
                oldEblanApplicationInfos - newEblanApplicationInfos.toSet()

            eblanApplicationInfoRepository.upsertEblanApplicationInfos(eblanApplicationInfos = newEblanApplicationInfos)

            eblanApplicationInfoRepository.deleteEblanApplicationInfos(eblanApplicationInfos = eblanApplicationInfosToDelete)

            eblanApplicationInfosToDelete.forEach { eblanApplicationInfoToDelete ->
                currentCoroutineContext().ensureActive()

                val isUnique = newEblanApplicationInfos.none { newEblanApplicationInfo ->
                    newEblanApplicationInfo.packageName == eblanApplicationInfoToDelete.packageName &&
                        newEblanApplicationInfo.serialNumber != eblanApplicationInfoToDelete.serialNumber
                }

                if (isUnique) {
                    val icon = File(
                        fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        eblanApplicationInfoToDelete.packageName,
                    )

                    if (icon.exists()) {
                        icon.delete()
                    }

                    val iconPacksDirectory = File(
                        fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                        iconPackInfoPackageName,
                    )

                    val iconPackFile =
                        File(iconPacksDirectory, eblanApplicationInfoToDelete.packageName)

                    if (iconPackFile.exists()) {
                        iconPackFile.delete()
                    }
                }
            }

            updateIconPackInfosUseCase(iconPackInfoPackageName = iconPackInfoPackageName)
        }
    }

    private suspend fun updateEblanAppWidgetProviderInfos(appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.map { appWidgetManagerAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                val preview =
                    appWidgetManagerAppWidgetProviderInfo.preview?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                            name = appWidgetManagerAppWidgetProviderInfo.componentName.replace(
                                "/",
                                "-",
                            ),
                            byteArray = byteArray,
                        )
                    }

                val label =
                    packageManagerWrapper.getApplicationLabel(packageName = appWidgetManagerAppWidgetProviderInfo.packageName)

                val icon =
                    packageManagerWrapper.getApplicationIcon(packageName = appWidgetManagerAppWidgetProviderInfo.packageName)
                        ?.let { byteArray ->
                            fileManager.updateAndGetFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                                name = appWidgetManagerAppWidgetProviderInfo.packageName,
                                byteArray = byteArray,
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
                    preview = preview,
                    label = label,
                    icon = icon,
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

            eblanAppWidgetProviderInfosToDelete.forEach { eblanAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                val icon = File(
                    fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                    eblanAppWidgetProviderInfo.packageName,
                )

                val widgetFile = File(
                    fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                    eblanAppWidgetProviderInfo.componentName.replace("/", "-"),
                )

                if (icon.exists()) {
                    icon.delete()
                }

                if (widgetFile.exists()) {
                    widgetFile.delete()
                }
            }
        }
    }

    private suspend fun updateEblanShortcutInfos(launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos = launcherAppsShortcutInfos?.map { launcherAppsShortcutInfo ->
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

        if (newEblanShortcutInfos != null && oldEblanShortcutInfos != newEblanShortcutInfos) {
            val eblanShortcutInfosToDelete =
                oldEblanShortcutInfos - newEblanShortcutInfos.toSet()

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfos,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfosToDelete,
            )

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfo ->
                currentCoroutineContext().ensureActive()

                val shortcutFile = File(
                    fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                    eblanShortcutInfo.shortcutId,
                )

                if (shortcutFile.exists()) {
                    shortcutFile.delete()
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

                    launcherAppsActivityInfo.packageName == applicationInfoGridItem.packageName &&
                        launcherAppsActivityInfo.serialNumber == applicationInfoGridItem.serialNumber
                }

            if (launcherAppsActivityInfo != null) {
                updateApplicationInfoGridItems.add(
                    UpdateApplicationInfoGridItem(
                        id = applicationInfoGridItem.id,
                        componentName = launcherAppsActivityInfo.componentName,
                        label = launcherAppsActivityInfo.label,
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

        val widgetGridItems =
            widgetGridItemRepository.widgetGridItems.first()

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

    suspend fun updateShortcutInfoGridItems(pinnedLauncherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val updateShortcutInfoGridItems = mutableListOf<UpdateShortcutInfoGridItem>()

        val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutInfoGridItems = shortcutInfoGridItemRepository.shortcutInfoGridItems.first()

        if (pinnedLauncherAppsShortcutInfos != null) {
            shortcutInfoGridItems.filterNot { shortcutInfoGridItem ->
                shortcutInfoGridItem.override
            }.forEach { shortcutInfoGridItem ->
                currentCoroutineContext().ensureActive()

                val launcherAppsShortcutInfo =
                    pinnedLauncherAppsShortcutInfos.find { launcherAppsShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        launcherAppsShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId &&
                            launcherAppsShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber
                    }

                if (launcherAppsShortcutInfo != null) {
                    val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                            name = launcherAppsShortcutInfo.shortcutId,
                            byteArray = byteArray,
                        )
                    }

                    updateShortcutInfoGridItems.add(
                        UpdateShortcutInfoGridItem(
                            id = shortcutInfoGridItem.id,
                            shortLabel = launcherAppsShortcutInfo.shortLabel,
                            longLabel = launcherAppsShortcutInfo.longLabel,
                            isEnabled = launcherAppsShortcutInfo.isEnabled,
                            disabledMessage = launcherAppsShortcutInfo.disabledMessage,
                            icon = icon,
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
