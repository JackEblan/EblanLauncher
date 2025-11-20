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
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
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
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
            ensureActive()

            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

            val iconByteArray = packageManagerWrapper.getApplicationIcon(packageName = packageName)

            val icon = iconByteArray?.let { currentIconByteArray ->
                fileManager.getAndUpdateFilePath(
                    directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                    name = packageName,
                    byteArray = currentIconByteArray,
                )
            }

            val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

            val eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            if (eblanApplicationInfo != null) {
                eblanApplicationInfoRepository.upsertEblanApplicationInfo(
                    eblanApplicationInfo = eblanApplicationInfo.copy(
                        componentName = componentName,
                        icon = icon,
                        label = label,
                    ),
                )
            }

            val launcherAppsActivityInfos = launcherAppsWrapper.getActivityList()

            val appWidgetManagerAppWidgetProviderInfos =
                appWidgetManagerWrapper.getInstalledProviders()

            updateEblanAppWidgetProviderInfosByPackageName(
                appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
                packageName = packageName,
            )

            updateEblanShortcutInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateApplicationInfoGridItemsByPackageName(
                launcherAppsActivityInfos = launcherAppsActivityInfos,
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateWidgetGridItemsByPackageName(
                appWidgetManagerAppWidgetProviderInfos = appWidgetManagerAppWidgetProviderInfos,
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateShortcutInfoGridItemsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateIconPackInfoByPackageNameUseCase(packageName = packageName)
        }
    }

    suspend fun updateApplicationInfoGridItemsByPackageName(
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        serialNumber: Long,
        packageName: String,
    ) {
        val updateApplicationInfoGridItems = mutableListOf<UpdateApplicationInfoGridItem>()

        val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val applicationInfoGridItems =
            applicationInfoGridItemRepository.getApplicationInfoGridItems(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        val launcherAppsActivityInfos =
            launcherAppsActivityInfos.filter { launcherAppsActivityInfo ->
                launcherAppsActivityInfo.serialNumber == serialNumber &&
                    launcherAppsActivityInfo.packageName == packageName
            }

        applicationInfoGridItems.filterNot { applicationInfoGridItem ->
            applicationInfoGridItem.override
        }.forEach { applicationInfoGridItem ->
            val launcherAppsActivityInfo =
                launcherAppsActivityInfos.find { launcherAppsActivityInfo ->
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

    private suspend fun updateEblanAppWidgetProviderInfosByPackageName(
        appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        withContext(ioDispatcher) {
            val oldEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfosByPackageName(
                    packageName = packageName,
                )

            val newEblanAppWidgetProviderInfos =
                appWidgetManagerAppWidgetProviderInfos
                    .filter { appWidgetManagerAppWidgetProviderInfo ->
                        appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                    }
                    .map { appWidgetManagerAppWidgetProviderInfo ->
                        val label =
                            packageManagerWrapper.getApplicationLabel(packageName = packageName)

                        val icon =
                            packageManagerWrapper.getApplicationIcon(packageName = packageName)
                                ?.let { byteArray ->
                                    fileManager.getAndUpdateFilePath(
                                        directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                                        name = appWidgetManagerAppWidgetProviderInfo.packageName,
                                        byteArray = byteArray,
                                    )
                                }

                        val preview =
                            appWidgetManagerAppWidgetProviderInfo.preview?.let { byteArray ->
                                fileManager.getAndUpdateFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                                    name = appWidgetManagerAppWidgetProviderInfo.className,
                                    byteArray = byteArray,
                                )
                            }

                        EblanAppWidgetProviderInfo(
                            className = appWidgetManagerAppWidgetProviderInfo.className,
                            componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
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
                    val icon = File(
                        fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        eblanAppWidgetProviderInfo.packageName,
                    )

                    val widgetFile = File(
                        fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                        eblanAppWidgetProviderInfo.className,
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
    }

    private suspend fun updateEblanShortcutInfosByPackageName(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) {
            return
        }

        withContext(ioDispatcher) {
            val oldEblanShortcutInfos = eblanShortcutInfoRepository.eblanShortcutInfos.first()

            val newEblanShortcutInfos =
                launcherAppsWrapper.getShortcutsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )?.map { launcherAppsShortcutInfo ->
                    ensureActive()

                    val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                        fileManager.getAndUpdateFilePath(
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
                    ensureActive()

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
    }

    private suspend fun updateWidgetGridItemsByPackageName(
        appWidgetManagerAppWidgetProviderInfos: List<AppWidgetManagerAppWidgetProviderInfo>,
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val updateWidgetGridItems = mutableListOf<UpdateWidgetGridItem>()

        val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

        val widgetGridItems =
            widgetGridItemRepository.getWidgetGridItems(packageName = packageName)

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }

        widgetGridItems.filterNot { widgetGridItem ->
            widgetGridItem.override
        }.forEach { widgetGridItem ->
            val appWidgetManagerAppWidgetProviderInfo =
                appWidgetManagerAppWidgetProviderInfos
                    .find { appWidgetManagerAppWidgetProviderInfo ->
                        appWidgetManagerAppWidgetProviderInfo.packageName == widgetGridItem.packageName &&
                            appWidgetManagerAppWidgetProviderInfo.className == widgetGridItem.className &&
                            serialNumber == widgetGridItem.serialNumber
                    }

            if (appWidgetManagerAppWidgetProviderInfo != null) {
                updateWidgetGridItems.add(
                    UpdateWidgetGridItem(
                        id = widgetGridItem.id,
                        className = appWidgetManagerAppWidgetProviderInfo.className,
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

        val launcherAppsShortcutInfos = launcherAppsWrapper.getPinnedShortcutsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        if (launcherAppsShortcutInfos != null) {
            shortcutInfoGridItems.filterNot { shortcutInfoGridItem ->
                shortcutInfoGridItem.override
            }.forEach { shortcutInfoGridItem ->
                val launcherAppsShortcutInfo =
                    launcherAppsShortcutInfos.find { launcherAppsShortcutInfo ->
                        launcherAppsShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId &&
                            launcherAppsShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber
                    }

                if (launcherAppsShortcutInfo != null) {
                    val icon = launcherAppsShortcutInfo.icon?.let { byteArray ->
                        fileManager.getAndUpdateFilePath(
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
