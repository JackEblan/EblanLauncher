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
package com.eblan.launcher.feature.pin

import android.os.UserHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.usecase.AddPinShortcutToHomeScreenUseCase
import com.eblan.launcher.domain.usecase.AddPinWidgetToHomeScreenUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemsAfterPinUseCase
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PinViewModel @Inject constructor(
    private val gridCacheRepository: GridCacheRepository,
    private val addPinShortcutToHomeScreenUseCase: AddPinShortcutToHomeScreenUseCase,
    private val addPinWidgetToHomeScreenUseCase: AddPinWidgetToHomeScreenUseCase,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val updateGridItemsAfterPinUseCase: UpdateGridItemsAfterPinUseCase,
    private val userManagerWrapper: AndroidUserManagerWrapper,
) : ViewModel() {
    private val _gridItem = MutableStateFlow<GridItem?>(null)

    val gridItem = _gridItem.asStateFlow()

    private val _isBoundWidget = MutableStateFlow(false)

    val isBoundWidget = _isBoundWidget.asStateFlow()

    private val _isFinished = MutableStateFlow(false)

    val isFinished = _isFinished.asStateFlow()

    fun addPinShortcutToHomeScreen(
        userHandle: UserHandle,
        id: String,
        packageName: String,
        shortLabel: String,
        longLabel: String,
        byteArray: ByteArray?,
    ) {
        viewModelScope.launch {
            _gridItem.update {
                addPinShortcutToHomeScreenUseCase(
                    shortcutId = id,
                    packageName = packageName,
                    serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = userHandle),
                    shortLabel = shortLabel,
                    longLabel = longLabel,
                    byteArray = byteArray,
                )
            }
        }
    }

    fun addPinWidgetToHomeScreen(
        userHandle: UserHandle,
        className: String,
        componentName: String,
        configure: String?,
        packageName: String,
        targetCellHeight: Int,
        targetCellWidth: Int,
        minWidth: Int,
        minHeight: Int,
        resizeMode: Int,
        minResizeWidth: Int,
        minResizeHeight: Int,
        maxResizeWidth: Int,
        maxResizeHeight: Int,
        rootWidth: Int,
        rootHeight: Int,
    ) {
        viewModelScope.launch {
            _gridItem.update {
                addPinWidgetToHomeScreenUseCase(
                    className = className,
                    componentName = componentName,
                    configure = configure,
                    packageName = packageName,
                    serialNumber = userManagerWrapper.getSerialNumberForUser(userHandle = userHandle),
                    targetCellHeight = targetCellHeight,
                    targetCellWidth = targetCellWidth,
                    minWidth = minWidth,
                    minHeight = minHeight,
                    resizeMode = resizeMode,
                    minResizeWidth = minResizeWidth,
                    minResizeHeight = minResizeHeight,
                    maxResizeWidth = maxResizeWidth,
                    maxResizeHeight = maxResizeHeight,
                    rootWidth = rootWidth,
                    rootHeight = rootHeight,
                )
            }
        }
    }

    fun updateGridItemDataCache(gridItem: GridItem) {
        viewModelScope.launch {
            gridCacheRepository.updateGridItemData(id = gridItem.id, data = gridItem.data)

            _isBoundWidget.update {
                true
            }
        }
    }

    fun deleteGridItemCache(gridItem: GridItem) {
        viewModelScope.launch {
            when (val data = gridItem.data) {
                is GridItemData.Widget -> {
                    appWidgetHostWrapper.deleteAppWidgetId(appWidgetId = data.appWidgetId)

                    gridCacheRepository.deleteGridItem(gridItem = gridItem)
                }

                else -> {
                    gridCacheRepository.deleteGridItem(gridItem = gridItem)
                }
            }

            _isFinished.update {
                true
            }
        }
    }

    fun updateGridItems() {
        viewModelScope.launch {
            updateGridItemsAfterPinUseCase()

            _isFinished.update {
                true
            }
        }
    }
}
