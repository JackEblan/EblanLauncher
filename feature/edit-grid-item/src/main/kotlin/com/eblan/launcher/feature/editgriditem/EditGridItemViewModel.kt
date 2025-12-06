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
package com.eblan.launcher.feature.editgriditem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.domain.usecase.GetGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemUseCase
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.feature.editgriditem.navigation.EditGridItemRouteData
import com.eblan.launcher.framework.packagemanager.AndroidPackageManagerWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditGridItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGridItemUseCase: GetGridItemUseCase,
    private val updateGridItemUseCase: UpdateGridItemUseCase,
    androidPackageManagerWrapper: AndroidPackageManagerWrapper,
) :
    ViewModel() {
    private val editGridItemRouteData = savedStateHandle.toRoute<EditGridItemRouteData>()

    private val _editGridItemUiState =
        MutableStateFlow<EditGridItemUiState>(EditGridItemUiState.Loading)

    val editGridItemUiState = _editGridItemUiState.onStart {
        getGridItem()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditGridItemUiState.Loading,
    )

    private val _packageManagerIconPackInfos =
        MutableStateFlow(emptyList<PackageManagerIconPackInfo>())

    val packageManagerIconPackInfos = _packageManagerIconPackInfos.onStart {
        _packageManagerIconPackInfos.update {
            androidPackageManagerWrapper.getIconPackInfos()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            updateGridItemUseCase(gridItem = gridItem)

            getGridItem()
        }
    }

    private fun getGridItem() {
        viewModelScope.launch {
            _editGridItemUiState.update {
                EditGridItemUiState.Success(
                    gridItem = getGridItemUseCase(id = editGridItemRouteData.id),
                )
            }
        }
    }
}
