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

import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.model.UpdateShortcutConfigGridItem
import com.eblan.launcher.domain.model.UpdateShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateWidgetGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.iconpack.cacheIconPackFile
import com.eblan.launcher.domain.usecase.iconpack.updateIconPackInfoByComponentName
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import java.io.File

internal suspend fun updateEblanApplicationInfos(
    launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
    iconPackInfoPackageName: String,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    launcherAppsWrapper: LauncherAppsWrapper,
    fileManager: FileManager,
    eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    packageManagerWrapper: PackageManagerWrapper,
    applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    iconPackManager: IconPackManager,
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

            val isUniquePackageName =
                newSyncEblanApplicationInfos.none { newSyncEblanApplicationInfo ->
                    currentCoroutineContext().ensureActive()

                    newSyncEblanApplicationInfo.packageName == eblanApplicationInfoToDelete.packageName
                }

            if (isUniquePackageName) {
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
                    eblanApplicationInfoToDelete.componentName.hashCode().toString(),
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

internal suspend fun updateEblanAppWidgetProviderInfos(
    appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
) {
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

internal suspend fun updateWidgetGridItems(
    appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    widgetGridItemRepository: WidgetGridItemRepository,
) {
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
                    icon = icon,
                    label = packageManagerWrapper.getApplicationLabel(
                        packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                    ).toString(),
                ),
            )
        } else {
            deleteWidgetGridItems.add(widgetGridItem)
        }
    }

    widgetGridItemRepository.updateWidgetGridItems(updateWidgetGridItems = updateWidgetGridItems)

    widgetGridItemRepository.deleteWidgetGridItemsByPackageName(widgetGridItems = deleteWidgetGridItems)
}

internal suspend fun updateEblanShortcutInfos(
    launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?,
    eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
) {
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
        updateShortcutInfoGridItems(
            launcherAppsShortcutInfos = launcherAppsShortcutInfos,
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }
}

internal suspend fun updateIconPackInfos(
    iconPackInfoPackageName: String,
    iconPackManager: IconPackManager,
    launcherAppsWrapper: LauncherAppsWrapper,
    fileManager: FileManager,
) {
    if (iconPackInfoPackageName.isNotEmpty()) {
        val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

        val appFilter = iconPackManager.parseAppFilter(packageName = iconPackInfoPackageName)

        val iconPackDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackInfoPackageName,
        ).apply { if (!exists()) mkdirs() }

        val installedPackageNames = buildList {
            launcherAppsActivityInfos.forEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

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

        iconPackDirectory.listFiles()?.filter { it.isFile && it.name !in installedPackageNames }
            ?.forEach {
                currentCoroutineContext().ensureActive()

                it.delete()
            }
    }
}

private suspend fun updateShortcutInfoGridItems(
    launcherAppsShortcutInfos: List<LauncherAppsShortcutInfo>?,
    shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
) {
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
                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val componentName =
                    packageManagerWrapper.getComponentName(packageName = launcherAppsShortcutInfo.packageName)

                val eblanApplicationInfoIcon = if (componentName != null) {
                    val file = File(
                        directory,
                        componentName.hashCode().toString(),
                    )

                    file.absolutePath
                } else {
                    val file = File(
                        directory,
                        launcherAppsShortcutInfo.packageName.hashCode().toString(),
                    )

                    packageManagerWrapper.getApplicationIcon(
                        packageName = launcherAppsShortcutInfo.packageName,
                        file = file,
                    )
                }

                updateShortcutInfoGridItems.add(
                    UpdateShortcutInfoGridItem(
                        id = shortcutInfoGridItem.id,
                        shortLabel = launcherAppsShortcutInfo.shortLabel,
                        longLabel = launcherAppsShortcutInfo.longLabel,
                        isEnabled = launcherAppsShortcutInfo.isEnabled,
                        icon = launcherAppsShortcutInfo.icon,
                        eblanApplicationInfoIcon = eblanApplicationInfoIcon,
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

private suspend fun updateEblanShortcutConfigs(
    eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    newEblanShortcutConfigs: List<EblanShortcutConfig>,
    shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
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

            val isUniqueComponentName =
                newEblanShortcutConfigs.none { newEblanShortcutConfig ->
                    currentCoroutineContext().ensureActive()

                    newEblanShortcutConfig.componentName == eblanShortcutConfigToDelete.componentName
                }

            val isUniquePackageName =
                newEblanShortcutConfigs.none { newEblanShortcutConfig ->
                    currentCoroutineContext().ensureActive()

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
            eblanShortcutConfigs = newEblanShortcutConfigs,
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
        )
    }
}

private suspend fun updateShortcutConfigGridItems(
    eblanShortcutConfigs: List<EblanShortcutConfig>,
    shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
) {
    val updateShortcutConfigGridItems = mutableListOf<UpdateShortcutConfigGridItem>()

    val deleteShortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

    val shortcutConfigGridItems =
        shortcutConfigGridItemRepository.shortcutConfigGridItems.first()

    shortcutConfigGridItems.filterNot { shortcutConfigGridItem ->
        shortcutConfigGridItem.override
    }.forEach { shortcutConfigGridItem ->
        currentCoroutineContext().ensureActive()

        val shortcutConfigActivityInfo =
            eblanShortcutConfigs.find { eblanShortcutConfig ->
                currentCoroutineContext().ensureActive()

                eblanShortcutConfig.componentName == shortcutConfigGridItem.componentName && eblanShortcutConfig.serialNumber == shortcutConfigGridItem.serialNumber
            }

        if (shortcutConfigActivityInfo != null) {
            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

            val componentName =
                packageManagerWrapper.getComponentName(packageName = shortcutConfigActivityInfo.packageName)

            val file = File(
                directory,
                componentName.hashCode().toString(),
            )

            updateShortcutConfigGridItems.add(
                UpdateShortcutConfigGridItem(
                    id = shortcutConfigGridItem.id,
                    componentName = shortcutConfigActivityInfo.componentName,
                    activityLabel = shortcutConfigActivityInfo.activityLabel,
                    activityIcon = shortcutConfigActivityInfo.activityIcon,
                    applicationLabel = packageManagerWrapper.getApplicationLabel(
                        packageName = shortcutConfigActivityInfo.packageName,
                    ).toString(),
                    applicationIcon = file.absolutePath,
                ),
            )
        } else {
            deleteShortcutConfigGridItems.add(shortcutConfigGridItem)
        }
    }

    shortcutConfigGridItemRepository.updateShortcutConfigGridItems(
        updateShortcutConfigGridItems = updateShortcutConfigGridItems,
    )

    shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(
        shortcutConfigGridItems = deleteShortcutConfigGridItems,
    )
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
