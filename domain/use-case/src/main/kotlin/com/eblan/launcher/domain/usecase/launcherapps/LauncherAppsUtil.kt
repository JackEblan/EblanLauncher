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
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateApplicationInfoGridItem
import com.eblan.launcher.domain.model.UpdateShortcutConfigGridItem
import com.eblan.launcher.domain.model.UpdateShortcutInfoGridItem
import com.eblan.launcher.domain.model.UpdateWidgetGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.iconpack.cacheIconPackFile
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import java.io.File

internal suspend fun updateApplicationInfoGridItems(
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

internal suspend fun updateShortcutInfoGridItems(
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

                    launcherAppsShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber &&
                        launcherAppsShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId
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

internal suspend fun updateShortcutConfigGridItems(
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

                eblanShortcutConfig.serialNumber == shortcutConfigGridItem.serialNumber &&
                    eblanShortcutConfig.componentName == shortcutConfigGridItem.componentName
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
                    targetCellHeight = appWidgetManagerAppWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = appWidgetManagerAppWidgetProviderInfo.targetCellWidth,
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
