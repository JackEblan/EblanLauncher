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
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData.ShortcutInfo
import com.eblan.launcher.domain.model.GridItemData.Widget
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetPinGridItemUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        pinItemRequestType: PinItemRequestType,
    ): GridItem {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            when (pinItemRequestType) {
                is PinItemRequestType.Widget -> {
                    val preview = pinItemRequestType.preview?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                            name = pinItemRequestType.componentName.replace("/", "-"),
                            byteArray = byteArray,
                        )
                    }

                    val label =
                        packageManagerWrapper.getApplicationLabel(packageName = pinItemRequestType.packageName)

                    val eblanApplicationInfoIcon =
                        packageManagerWrapper.getApplicationIcon(packageName = pinItemRequestType.packageName)
                            ?.let { byteArray ->
                                fileManager.updateAndGetFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                                    name = pinItemRequestType.packageName,
                                    byteArray = byteArray,
                                )
                            }

                    val data = Widget(
                        appWidgetId = 0,
                        componentName = pinItemRequestType.componentName,
                        packageName = pinItemRequestType.packageName,
                        serialNumber = pinItemRequestType.serialNumber,
                        configure = pinItemRequestType.configure,
                        minWidth = pinItemRequestType.minWidth,
                        minHeight = pinItemRequestType.minHeight,
                        resizeMode = pinItemRequestType.resizeMode,
                        minResizeWidth = pinItemRequestType.minResizeWidth,
                        minResizeHeight = pinItemRequestType.minResizeHeight,
                        maxResizeWidth = pinItemRequestType.maxResizeWidth,
                        maxResizeHeight = pinItemRequestType.maxResizeHeight,
                        targetCellHeight = pinItemRequestType.targetCellHeight,
                        targetCellWidth = pinItemRequestType.targetCellWidth,
                        preview = preview,
                        label = label,
                        icon = eblanApplicationInfoIcon,
                    )

                    GridItem(
                        id = Uuid.random()
                            .toHexString(),
                        folderId = null,
                        page = homeSettings.initialPage,
                        startColumn = 0,
                        startRow = 0,
                        columnSpan = 1,
                        rowSpan = 1,
                        data = data,
                        associate = Associate.Grid,
                        override = false,
                        gridItemSettings = homeSettings.gridItemSettings,
                    )
                }

                is PinItemRequestType.ShortcutInfo -> {
                    val icon = pinItemRequestType.icon?.let { byteArray ->
                        fileManager.updateAndGetFilePath(
                            directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR),
                            name = pinItemRequestType.shortcutId,
                            byteArray = byteArray,
                        )
                    }

                    val eblanApplicationInfoIcon =
                        packageManagerWrapper.getApplicationIcon(packageName = pinItemRequestType.packageName)
                            ?.let { byteArray ->
                                fileManager.updateAndGetFilePath(
                                    directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR),
                                    name = pinItemRequestType.packageName,
                                    byteArray = byteArray,
                                )
                            }

                    val data = ShortcutInfo(
                        shortcutId = pinItemRequestType.shortcutId,
                        packageName = pinItemRequestType.packageName,
                        serialNumber = pinItemRequestType.serialNumber,
                        shortLabel = pinItemRequestType.shortLabel,
                        longLabel = pinItemRequestType.longLabel,
                        icon = icon,
                        isEnabled = pinItemRequestType.isEnabled,
                        disabledMessage = pinItemRequestType.disabledMessage,
                        eblanApplicationInfoIcon = eblanApplicationInfoIcon,
                        customIcon = null,
                        customLabel = null,
                    )

                    GridItem(
                        id = pinItemRequestType.shortcutId,
                        folderId = null,
                        page = homeSettings.initialPage,
                        startColumn = 0,
                        startRow = 0,
                        columnSpan = 1,
                        rowSpan = 1,
                        data = data,
                        associate = Associate.Grid,
                        override = false,
                        gridItemSettings = homeSettings.gridItemSettings,
                    )
                }
            }
        }
    }
}
