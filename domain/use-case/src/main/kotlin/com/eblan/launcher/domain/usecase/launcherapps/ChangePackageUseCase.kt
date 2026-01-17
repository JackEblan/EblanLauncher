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
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
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
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.iconpack.UpdateIconPackInfoByPackageNameUseCase
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
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            launcherAppsWrapper.getActivityList(
                serialNumber = serialNumber,
                packageName = packageName,
            ).forEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                updateEblanApplicationInfoByPackageName(
                    componentName = launcherAppsActivityInfo.componentName,
                    packageName = launcherAppsActivityInfo.packageName,
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    icon = launcherAppsActivityInfo.activityIcon,
                    label = launcherAppsActivityInfo.activityLabel,
                )

                updateEblanAppWidgetProviderInfosByPackageName(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    packageName = launcherAppsActivityInfo.packageName,
                )

                updateShortcutConfigGridItemsByPackageName(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    packageName = launcherAppsActivityInfo.packageName,
                )

                updateEblanShortcutConfigs(
                    serialNumber = launcherAppsActivityInfo.serialNumber,
                    packageName = launcherAppsActivityInfo.packageName,
                )

                updateIconPackInfoByPackageNameUseCase(
                    packageName = launcherAppsActivityInfo.packageName,
                    componentName = launcherAppsActivityInfo.componentName,
                )
            }

            updateEblanShortcutInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateShortcutInfoGridItemsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun updateEblanApplicationInfoByPackageName(
        componentName: String,
        packageName: String,
        serialNumber: Long,
        icon: String?,
        label: String,
    ) {
        val eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        if (eblanApplicationInfo != null) {
            eblanApplicationInfoRepository.updateEblanApplicationInfo(
                eblanApplicationInfo = eblanApplicationInfo.copy(
                    componentName = componentName,
                    icon = icon,
                    label = label,
                ),
            )

            updateApplicationInfoGridItemsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
                componentName = componentName,
                icon = icon,
                label = label,
            )
        }
    }

    private suspend fun updateEblanAppWidgetProviderInfosByPackageName(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProviders()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfosByPackageName(
                packageName = packageName,
            )

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.filter { appWidgetManagerAppWidgetProviderInfo ->
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

        updateWidgetGridItemsByPackageName(
            appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
            serialNumber = serialNumber,
            packageName = packageName,
        )
    }

    private suspend fun updateEblanShortcutInfosByPackageName(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

        val newEblanShortcutInfos = launcherAppsWrapper.getShortcutsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )?.map { launcherAppsShortcutInfo ->
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

            eblanShortcutInfosToDelete.forEach { eblanShortcutInfo ->
                currentCoroutineContext().ensureActive()

                val isUnique = eblanShortcutInfoRepository.eblanShortcutInfos.first()
                    .none { newEblanShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        newEblanShortcutInfo.packageName == packageName && newEblanShortcutInfo.serialNumber != serialNumber
                    }

                if (isUnique) {
                    eblanShortcutInfo.icon?.let { icon ->
                        val iconFile = File(icon)

                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateApplicationInfoGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
        componentName: String?,
        icon: String?,
        label: String?,
    ) {
        val updateApplicationInfoGridItems =
            applicationInfoGridItemRepository.getApplicationInfoGridItems(
                serialNumber = serialNumber,
                packageName = packageName,
            ).filterNot { applicationInfoGridItem ->
                applicationInfoGridItem.override
            }.mapNotNull { applicationInfoGridItem ->
                currentCoroutineContext().ensureActive()

                if (componentName != null) {
                    UpdateApplicationInfoGridItem(
                        id = applicationInfoGridItem.id,
                        componentName = componentName,
                        icon = icon,
                        label = label,
                    )
                } else {
                    null
                }
            }

        applicationInfoGridItemRepository.updateApplicationInfoGridItems(
            updateApplicationInfoGridItems = updateApplicationInfoGridItems,
        )
    }

    private suspend fun updateWidgetGridItemsByPackageName(
        appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>,
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val updateWidgetGridItems = mutableListOf<UpdateWidgetGridItem>()

        val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

        val widgetGridItems = widgetGridItemRepository.getWidgetGridItems(packageName = packageName)

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.filter { appWidgetManagerAppWidgetProviderInfo ->
                appWidgetManagerAppWidgetProviderInfo.packageName == packageName
            }

        widgetGridItems.filterNot { widgetGridItem ->
            widgetGridItem.override
        }.forEach { widgetGridItem ->
            currentCoroutineContext().ensureActive()

            val appWidgetManagerAppWidgetProviderInfo =
                appWidgetManagerAppWidgetProviderInfos.find { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.packageName == widgetGridItem.packageName && appWidgetManagerAppWidgetProviderInfo.componentName == widgetGridItem.componentName && serialNumber == widgetGridItem.serialNumber
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

    private suspend fun updateShortcutInfoGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val updateShortcutInfoGridItems = mutableListOf<UpdateShortcutInfoGridItem>()

        val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcutsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        if (launcherAppsShortcutInfos != null) {
            shortcutInfoGridItems.filterNot { shortcutInfoGridItem ->
                shortcutInfoGridItem.override
            }.forEach { shortcutInfoGridItem ->
                currentCoroutineContext().ensureActive()

                val launcherAppsShortcutInfo =
                    launcherAppsShortcutInfos.find { launcherAppsShortcutInfo ->
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

    private suspend fun updateShortcutConfigGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
    ) {
        val updateShortcutConfigGridItems = mutableListOf<UpdateShortcutConfigGridItem>()

        val deleteShortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

        val shortcutConfigGridItems = shortcutConfigGridItemRepository.getShortcutConfigGridItems(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        val shortcutConfigActivityInfos = launcherAppsWrapper.getShortcutConfigActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        shortcutConfigGridItems.filterNot { shortcutConfigGridItem ->
            shortcutConfigGridItem.override
        }.forEach { shortcutConfigGridItem ->
            currentCoroutineContext().ensureActive()

            val launcherAppsActivityInfo = shortcutConfigActivityInfos.find { shortcutConfigActivityInfo ->
                shortcutConfigActivityInfo.componentName == shortcutConfigGridItem.componentName && shortcutConfigActivityInfo.serialNumber == shortcutConfigGridItem.serialNumber
            }

            if (launcherAppsActivityInfo != null) {
                val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                val file = File(
                    directory,
                    launcherAppsActivityInfo.componentName.hashCode().toString(),
                )

                val applicationIcon = if (file.exists()) {
                    file.absolutePath
                } else {
                    packageManagerWrapper.getApplicationIcon(
                        packageName = launcherAppsActivityInfo.packageName,
                        file = file,
                    )
                }

                updateShortcutConfigGridItems.add(
                    UpdateShortcutConfigGridItem(
                        id = shortcutConfigGridItem.id,
                        componentName = launcherAppsActivityInfo.componentName,
                        activityIcon = launcherAppsActivityInfo.activityIcon,
                        activityLabel = launcherAppsActivityInfo.activityLabel,
                        applicationIcon = applicationIcon,
                        applicationLabel = packageManagerWrapper.getApplicationLabel(
                            packageName = launcherAppsActivityInfo.packageName,
                        ),
                    ),
                )
            } else {
                deleteShortcutConfigGridItems.add(shortcutConfigGridItem)
            }
        }

        deleteShortcutConfigGridItems.forEach { shortcutConfigGridItem ->
            currentCoroutineContext().ensureActive()

            shortcutConfigGridItem.shortcutIntentIcon?.let { shortcutIntentIcon ->
                val shortcutIntentIconFile = File(shortcutIntentIcon)

                if (shortcutIntentIconFile.exists()) {
                    shortcutIntentIconFile.delete()
                }
            }
        }

        shortcutConfigGridItemRepository.updateShortcutConfigGridItems(
            updateShortcutConfigGridItems = updateShortcutConfigGridItems,
        )

        shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(
            shortcutConfigGridItems = shortcutConfigGridItems,
        )
    }

    private suspend fun updateEblanShortcutConfigs(
        serialNumber: Long,
        packageName: String,
    ) {
        val oldEblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfig(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        val newEblanShortcutConfigs = launcherAppsWrapper.getShortcutConfigActivityList(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map { shortcutConfigActivityInfo ->
            currentCoroutineContext().ensureActive()

            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

            val file = File(
                directory,
                shortcutConfigActivityInfo.componentName.hashCode().toString(),
            )

            val applicationIcon = if (file.exists()) {
                file.absolutePath
            } else {
                packageManagerWrapper.getApplicationIcon(
                    packageName = shortcutConfigActivityInfo.packageName,
                    file = file,
                )
            }

            EblanShortcutConfig(
                componentName = shortcutConfigActivityInfo.componentName,
                packageName = shortcutConfigActivityInfo.packageName,
                serialNumber = shortcutConfigActivityInfo.serialNumber,
                activityIcon = shortcutConfigActivityInfo.activityIcon,
                activityLabel = shortcutConfigActivityInfo.activityLabel,
                applicationIcon = applicationIcon,
                applicationLabel = packageManagerWrapper.getApplicationLabel(
                    packageName = shortcutConfigActivityInfo.packageName,
                ),
                lastUpdateTime = shortcutConfigActivityInfo.lastUpdateTime,
            )
        }

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
                    currentCoroutineContext().ensureActive()

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
}
