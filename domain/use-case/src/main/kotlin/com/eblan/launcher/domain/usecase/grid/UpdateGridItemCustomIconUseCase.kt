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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.dispatcher.Dispatcher
import com.eblan.launcher.domain.common.dispatcher.EblanDispatchers
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateGridItemCustomIconUseCase @Inject constructor(
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        gridItem: GridItem,
        byteArray: ByteArray,
    ): GridItem {
        return withContext(defaultDispatcher) {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    val customIcon = fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.CUSTOM_ICONS_DIR),
                        name = gridItem.id,
                        byteArray = byteArray,
                    )

                    val newData = data.copy(customIcon = customIcon)

                    gridItem.copy(data = newData)
                }

                is GridItemData.Folder -> {
                    val icon = fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.CUSTOM_ICONS_DIR),
                        name = gridItem.id,
                        byteArray = byteArray,
                    )

                    val newData = data.copy(icon = icon)

                    gridItem.copy(data = newData)
                }

                is GridItemData.ShortcutConfig -> {
                    val customIcon = fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.CUSTOM_ICONS_DIR),
                        name = gridItem.id,
                        byteArray = byteArray,
                    )

                    val newData = data.copy(customIcon = customIcon)

                    gridItem.copy(data = newData)
                }

                is GridItemData.ShortcutInfo -> {
                    val customIcon = fileManager.updateAndGetFilePath(
                        directory = fileManager.getFilesDirectory(FileManager.CUSTOM_ICONS_DIR),
                        name = gridItem.id,
                        byteArray = byteArray,
                    )

                    val newData = data.copy(customIcon = customIcon)

                    gridItem.copy(data = newData)
                }

                else -> gridItem
            }
        }
    }
}
