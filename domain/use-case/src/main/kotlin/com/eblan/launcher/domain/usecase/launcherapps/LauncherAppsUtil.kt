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
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
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
    eblanApplicationInfos: List<EblanApplicationInfo>,
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

        val eblanApplicationInfo =
            eblanApplicationInfos.find { eblanApplicationInfo ->
                currentCoroutineContext().ensureActive()

                eblanApplicationInfo.serialNumber == applicationInfoGridItem.serialNumber &&
                    eblanApplicationInfo.componentName == applicationInfoGridItem.componentName
            }

        if (eblanApplicationInfo != null) {
            updateApplicationInfoGridItems.add(
                UpdateApplicationInfoGridItem(
                    id = applicationInfoGridItem.id,
                    componentName = eblanApplicationInfo.componentName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
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
    eblanShortcutInfos: List<EblanShortcutInfo>?,
    shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
) {
    val updateShortcutInfoGridItems = mutableListOf<UpdateShortcutInfoGridItem>()

    val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

    val shortcutInfoGridItems = shortcutInfoGridItemRepository.shortcutInfoGridItems.first()

    if (eblanShortcutInfos != null) {
        shortcutInfoGridItems.filterNot { shortcutInfoGridItem ->
            shortcutInfoGridItem.override
        }.forEach { shortcutInfoGridItem ->
            currentCoroutineContext().ensureActive()

            val eblanShortcutInfo =
                eblanShortcutInfos.find { eblanShortcutInfo ->
                    currentCoroutineContext().ensureActive()

                    eblanShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber &&
                        eblanShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId
                }

            if (eblanShortcutInfo != null) {
                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val componentName =
                    packageManagerWrapper.getComponentName(packageName = eblanShortcutInfo.packageName)

                val eblanApplicationInfoIcon = if (componentName != null) {
                    val file = File(
                        directory,
                        componentName.hashCode().toString(),
                    )

                    file.absolutePath
                } else {
                    val file = File(
                        directory,
                        eblanShortcutInfo.packageName.hashCode().toString(),
                    )

                    packageManagerWrapper.getApplicationIcon(
                        packageName = eblanShortcutInfo.packageName,
                        file = file,
                    )
                }

                updateShortcutInfoGridItems.add(
                    UpdateShortcutInfoGridItem(
                        id = shortcutInfoGridItem.id,
                        shortLabel = eblanShortcutInfo.shortLabel,
                        longLabel = eblanShortcutInfo.longLabel,
                        isEnabled = eblanShortcutInfo.isEnabled,
                        icon = eblanShortcutInfo.icon,
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
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
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

        val eblanAppWidgetProviderInfo =
            eblanAppWidgetProviderInfos.find { eblanAppWidgetProviderInfo ->
                currentCoroutineContext().ensureActive()

                eblanAppWidgetProviderInfo.serialNumber == widgetGridItem.serialNumber &&
                    eblanAppWidgetProviderInfo.componentName == widgetGridItem.componentName
            }

        if (eblanAppWidgetProviderInfo != null) {
            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

            val componentName =
                packageManagerWrapper.getComponentName(packageName = eblanAppWidgetProviderInfo.packageName)

            val icon = if (componentName != null) {
                val file = File(
                    directory,
                    componentName.hashCode().toString(),
                )

                file.absolutePath
            } else {
                val file = File(
                    directory,
                    eblanAppWidgetProviderInfo.packageName.hashCode().toString(),
                )

                packageManagerWrapper.getApplicationIcon(
                    packageName = eblanAppWidgetProviderInfo.packageName,
                    file = file,
                )
            }

            updateWidgetGridItems.add(
                UpdateWidgetGridItem(
                    id = widgetGridItem.id,
                    componentName = eblanAppWidgetProviderInfo.componentName,
                    configure = eblanAppWidgetProviderInfo.configure,
                    minWidth = eblanAppWidgetProviderInfo.minWidth,
                    minHeight = eblanAppWidgetProviderInfo.minHeight,
                    resizeMode = eblanAppWidgetProviderInfo.resizeMode,
                    minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
                    targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                    icon = icon,
                    label = packageManagerWrapper.getApplicationLabel(
                        packageName = eblanAppWidgetProviderInfo.packageName,
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
