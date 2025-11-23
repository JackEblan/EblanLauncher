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
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
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
    private val updateEblanShortcutConfigActivitiesUseCase: UpdateEblanShortcutConfigActivitiesUseCase,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            if (!userDataRepository.userData.first().experimentalSettings.syncData) return@withContext

            val icon = packageManagerWrapper.getApplicationIcon(packageName = packageName)
                ?.let { currentIconByteArray ->
                    fileManager.getAndUpdateFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                        name = packageName,
                        byteArray = currentIconByteArray,
                    )
                }

            val label = packageManagerWrapper.getApplicationLabel(packageName = packageName)

            updateEblanApplicationInfoByPackageName(
                packageName = packageName,
                serialNumber = serialNumber,
                label = label,
                icon = icon,
            )

            updateEblanAppWidgetProviderInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
                label = label,
                icon = icon,
            )

            updateEblanShortcutInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateShortcutInfoGridItemsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateEblanShortcutConfigActivitiesUseCase(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateIconPackInfoByPackageNameUseCase(packageName = packageName)
        }
    }

    private suspend fun updateEblanApplicationInfoByPackageName(
        packageName: String,
        serialNumber: Long,
        label: String?,
        icon: String?,
    ) {
        val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

        val eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        if (eblanApplicationInfo != null && componentName != null) {
            eblanApplicationInfoRepository.upsertEblanApplicationInfo(
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
                label = label,
            )
        }
    }

    private suspend fun updateEblanAppWidgetProviderInfosByPackageName(
        serialNumber: Long,
        packageName: String,
        label: String?,
        icon: String?,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getInstalledProviders()

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
                        serialNumber = serialNumber,
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

        val newEblanShortcutInfos =
            launcherAppsWrapper.getShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )?.map { launcherAppsShortcutInfo ->
                currentCoroutineContext().ensureActive()

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
                currentCoroutineContext().ensureActive()

                val isUnique = eblanShortcutInfoRepository.eblanShortcutInfos.first()
                    .none { newEblanShortcutInfo ->
                        currentCoroutineContext().ensureActive()

                        newEblanShortcutInfo.packageName == packageName &&
                            newEblanShortcutInfo.serialNumber != serialNumber
                    }

                if (isUnique) {
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

    suspend fun updateApplicationInfoGridItemsByPackageName(
        serialNumber: Long,
        packageName: String,
        componentName: String?,
        label: String?,
    ) {
        val applicationInfoGridItems =
            applicationInfoGridItemRepository.getApplicationInfoGridItems(
                serialNumber = serialNumber,
                packageName = packageName,
            ).filterNot { applicationInfoGridItem ->
                applicationInfoGridItem.override
            }.mapNotNull { applicationInfoGridItem ->
                if (componentName != null) {
                    applicationInfoGridItem.copy(
                        componentName = componentName,
                        label = label,
                    )
                } else {
                    null
                }
            }

        applicationInfoGridItemRepository.updateApplicationInfoGridItems(
            applicationInfoGridItems = applicationInfoGridItems,
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
