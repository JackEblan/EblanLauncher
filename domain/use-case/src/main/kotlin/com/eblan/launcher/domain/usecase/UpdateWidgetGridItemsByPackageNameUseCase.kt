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
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.UpdateWidgetGridItem
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateWidgetGridItemsByPackageNameUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        withContext(defaultDispatcher) {
            val updateWidgetGridItems = mutableListOf<UpdateWidgetGridItem>()

            val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

            val widgetGridItems =
                widgetGridItemRepository.getWidgetGridItems(packageName = packageName)

            val appWidgetManagerAppWidgetProviderInfos =
                appWidgetManagerWrapper.getInstalledProviders()
                    .filter { appWidgetManagerAppWidgetProviderInfo ->
                        appWidgetManagerAppWidgetProviderInfo.packageName == packageName
                    }

            widgetGridItems.forEach { widgetGridItem ->
                val appWidgetManagerAppWidgetProviderInfo =
                    appWidgetManagerAppWidgetProviderInfos
                        .find { appWidgetManagerAppWidgetProviderInfo ->
                            appWidgetManagerAppWidgetProviderInfo.packageName == widgetGridItem.packageName &&
                                appWidgetManagerAppWidgetProviderInfo.className == widgetGridItem.className &&
                                serialNumber == widgetGridItem.serialNumber
                        }

                if (appWidgetManagerAppWidgetProviderInfo != null) {
                    val preview =
                        appWidgetManagerAppWidgetProviderInfo.preview?.let { byteArray ->
                            fileManager.getAndUpdateFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                                name = appWidgetManagerAppWidgetProviderInfo.className,
                                byteArray = byteArray,
                            )
                        }

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
                            preview = preview,
                        ),
                    )
                } else {
                    deleteWidgetGridItems.add(widgetGridItem)
                }
            }

            widgetGridItemRepository.updateWidgetGridItems(updateWidgetGridItems = updateWidgetGridItems)

            widgetGridItemRepository.deleteWidgetGridItemsByPackageName(widgetGridItems = deleteWidgetGridItems)
        }
    }
}
