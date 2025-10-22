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
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData.ShortcutInfo
import com.eblan.launcher.domain.model.GridItemData.Widget
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetPinGridItemUseCase @Inject constructor(
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    @Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        serialNumber: Long,
        pinItemRequestType: PinItemRequestType,
    ): GridItem? {
        return withContext(defaultDispatcher) {
            val homeSettings = userDataRepository.userData.first().homeSettings

            when (pinItemRequestType) {
                is PinItemRequestType.Widget -> {
                    val eblanAppWidgetProviderInfo =
                        eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfo(className = pinItemRequestType.className)

                    if (eblanAppWidgetProviderInfo != null) {
                        val data = Widget(
                            appWidgetId = 0,
                            componentName = eblanAppWidgetProviderInfo.componentName,
                            packageName = eblanAppWidgetProviderInfo.packageName,
                            serialNumber = serialNumber,
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
                            preview = eblanAppWidgetProviderInfo.preview,
                            eblanApplicationInfo = eblanAppWidgetProviderInfo.eblanApplicationInfo,
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
                    } else {
                        null
                    }
                }

                is PinItemRequestType.ShortcutInfo -> {
                    val eblanApplicationInfo =
                        eblanApplicationInfoRepository.getEblanApplicationInfo(packageName = pinItemRequestType.packageName)

                    if (eblanApplicationInfo != null) {
                        val icon = pinItemRequestType.icon?.let { byteArray ->
                            fileManager.getAndUpdateFilePath(
                                directory = fileManager.getFilesDirectory(FileManager.WIDGETS_DIR),
                                name = pinItemRequestType.shortcutId,
                                byteArray = byteArray,
                            )
                        }

                        val data = ShortcutInfo(
                            shortcutId = pinItemRequestType.shortcutId,
                            packageName = pinItemRequestType.packageName,
                            serialNumber = 0L,
                            shortLabel = pinItemRequestType.shortLabel,
                            longLabel = pinItemRequestType.longLabel,
                            icon = icon,
                            eblanApplicationInfo = eblanApplicationInfo,
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
                    } else {
                        null
                    }
                }
            }
        }
    }
}
