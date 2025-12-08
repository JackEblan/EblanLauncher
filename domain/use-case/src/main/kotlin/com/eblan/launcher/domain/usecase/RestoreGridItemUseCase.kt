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
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class RestoreGridItemUseCase @Inject constructor(
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(gridItem: GridItem): GridItem {
        return withContext(ioDispatcher) {
            when (val data = gridItem.data) {
                is GridItemData.ApplicationInfo -> {
                    data.customIcon?.let { customIcon ->
                        val customIconFile = File(customIcon)

                        if (customIconFile.exists()) {
                            File(customIcon).delete()
                        }
                    }

                    val newData = data.copy(
                        customIcon = null,
                        customLabel = null,
                    )

                    gridItem.copy(data = newData)
                }

                is GridItemData.ShortcutConfig -> {
                    data.customIcon?.let { customIcon ->
                        val customIconFile = File(customIcon)

                        if (customIconFile.exists()) {
                            File(customIcon).delete()
                        }
                    }

                    val newData = data.copy(
                        customIcon = null,
                        customLabel = null,
                    )

                    gridItem.copy(data = newData)
                }

                is GridItemData.ShortcutInfo -> {
                    data.customIcon?.let { customIcon ->
                        val customIconFile = File(customIcon)

                        if (customIconFile.exists()) {
                            File(customIcon).delete()
                        }
                    }

                    val newData = data.copy(
                        customIcon = null,
                        customShortLabel = null,
                    )

                    gridItem.copy(data = newData)
                }

                else -> gridItem
            }
        }
    }
}
