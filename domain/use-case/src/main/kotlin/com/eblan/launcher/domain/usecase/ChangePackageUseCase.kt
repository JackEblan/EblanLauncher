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
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class ChangePackageUseCase @Inject constructor(
    private val packageManagerWrapper: PackageManagerWrapper,
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val updateIconPackInfoByPackageNameUseCase: UpdateIconPackInfoByPackageNameUseCase,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(defaultDispatcher) {
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

            val eblanApplicationInfo = EblanApplicationInfo(
                serialNumber = serialNumber,
                componentName = componentName,
                packageName = packageName,
                icon = icon,
                label = label,
            )

            eblanApplicationInfoRepository.upsertEblanApplicationInfo(eblanApplicationInfo = eblanApplicationInfo)

            updateApplicationInfoGridItems(
                serialNumber = serialNumber,
                componentName = componentName,
                packageName = packageName,
                icon = icon,
                label = label,
            )

            updateShortcutInfoGridItem(
                packageName = packageName,
                serialNumber = serialNumber,
            )

            updateEblanAppWidgetProviderInfos(packageName = packageName)

            updateIconPackInfoByPackageNameUseCase(packageName = packageName)
        }
    }

    private suspend fun updateApplicationInfoGridItems(
        serialNumber: Long,
        componentName: String?,
        packageName: String,
        icon: String?,
        label: String?,
    ) {
        applicationInfoGridItemRepository.getApplicationInfoGridItems(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { applicationInfoGridItem ->
            applicationInfoGridItemRepository.updateApplicationInfoGridItem(
                applicationInfoGridItem = applicationInfoGridItem.copy(
                    serialNumber = serialNumber,
                    componentName = componentName,
                    icon = icon,
                    label = label,
                ),
            )
        }
    }

    private suspend fun updateEblanAppWidgetProviderInfos(packageName: String) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) {
            return
        }

        val oldEblanApplicationInfos =
            eblanApplicationInfoRepository.eblanApplicationInfos.first()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfos.first()
                .filter { eblanAppWidgetProviderInfo ->
                    eblanAppWidgetProviderInfo.packageName == packageName
                }

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerWrapper.getInstalledProviders()
                .filter { appWidgetManagerAppWidgetProviderInfo ->
                    appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                }
                .mapNotNull { appWidgetManagerAppWidgetProviderInfo ->
                    val eblanApplicationInfo =
                        oldEblanApplicationInfos.find { eblanApplicationInfo ->
                            eblanApplicationInfo.packageName == appWidgetManagerAppWidgetProviderInfo.packageName
                        }

                    if (eblanApplicationInfo != null) {
                        val preview =
                            appWidgetManagerAppWidgetProviderInfo.preview?.let { currentPreview ->
                                fileManager.getAndUpdateFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                                    name = appWidgetManagerAppWidgetProviderInfo.className,
                                    byteArray = currentPreview,
                                )
                            }

                        EblanAppWidgetProviderInfo(
                            className = appWidgetManagerAppWidgetProviderInfo.className,
                            componentName = appWidgetManagerAppWidgetProviderInfo.componentName,
                            configure = appWidgetManagerAppWidgetProviderInfo.configure,
                            packageName = appWidgetManagerAppWidgetProviderInfo.packageName,
                            serialNumber = appWidgetManagerAppWidgetProviderInfo.serialNumber,
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
                            eblanApplicationInfo = eblanApplicationInfo,
                        )
                    } else {
                        null
                    }
                }.onEach { eblanAppWidgetProviderInfo ->
                    updateWidgetGridItem(
                        packageName = packageName,
                        eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfo,
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
                val widgetFile = File(
                    fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                    eblanAppWidgetProviderInfo.className,
                )

                if (widgetFile.exists()) {
                    widgetFile.delete()
                }
            }
        }
    }

    private suspend fun updateWidgetGridItem(
        packageName: String,
        eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo,
    ) {
        widgetGridItemRepository.getWidgetGridItems(packageName = packageName)
            .forEach { widgetGridItem ->
                widgetGridItemRepository.updateWidgetGridItem(
                    widgetGridItem = widgetGridItem.copy(
                        componentName = eblanAppWidgetProviderInfo.componentName,
                        configure = eblanAppWidgetProviderInfo.configure,
                        packageName = eblanAppWidgetProviderInfo.packageName,
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                        targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                        targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                        minWidth = eblanAppWidgetProviderInfo.minWidth,
                        minHeight = eblanAppWidgetProviderInfo.minHeight,
                        resizeMode = eblanAppWidgetProviderInfo.resizeMode,
                        minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
                        minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
                        maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
                        maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
                    ),
                )
            }
    }

    private suspend fun updateShortcutInfoGridItem(
        packageName: String,
        serialNumber: Long,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val updateShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

        val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems(
            serialNumber = serialNumber,
            packageName = packageName,
        )

        val launcherAppsShortcutInfos =
            launcherAppsWrapper.getPinnedShortcutsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        if (launcherAppsShortcutInfos != null) {
            shortcutInfoGridItems.forEach { shortcutInfoGridItem ->
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
                        shortcutInfoGridItem.copy(
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

            shortcutInfoGridItemRepository.updateShortcutInfoGridItems(shortcutInfoGridItems = updateShortcutInfoGridItems)

            shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = deleteShortcutInfoGridItems)
        }
    }
}
