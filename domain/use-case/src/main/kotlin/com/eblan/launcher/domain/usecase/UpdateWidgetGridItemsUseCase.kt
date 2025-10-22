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
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateWidgetGridItemsUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    @Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        withContext(ioDispatcher) {
            val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

            val widgetGridItems =
                widgetGridItemRepository.widgetGridItems.first()

            val appWidgetManagerAppWidgetProviderInfos =
                appWidgetManagerWrapper.getInstalledProviders()

            val updatedWidgetGridItems =
                widgetGridItems.mapNotNull { widgetGridItem ->
                    val appWidgetManagerAppWidgetProviderInfo =
                        appWidgetManagerAppWidgetProviderInfos.find { appWidgetManagerAppWidgetProviderInfo ->
                            appWidgetManagerAppWidgetProviderInfo.className == widgetGridItem.className
                        }

                    if (appWidgetManagerAppWidgetProviderInfo != null) {
                        val preview =
                            appWidgetManagerAppWidgetProviderInfo.preview?.let { byteArray ->
                                fileManager.getAndUpdateFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                                    name = appWidgetManagerAppWidgetProviderInfo.className,
                                    byteArray = byteArray,
                                )
                            }

                        widgetGridItem.copy(
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
                        )
                    } else {
                        deleteWidgetGridItems.add(widgetGridItem)

                        null
                    }
                }

            widgetGridItemRepository.updateWidgetGridItems(
                widgetGridItems = updatedWidgetGridItems,
            )

            widgetGridItemRepository.deleteWidgetGridItems(
                widgetGridItems = deleteWidgetGridItems,
            )
        }
    }
}
