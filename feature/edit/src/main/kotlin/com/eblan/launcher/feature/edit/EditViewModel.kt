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
package com.eblan.launcher.feature.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.usecase.GetGridItemUseCase
import com.eblan.launcher.domain.usecase.UpdateGridItemUseCase
import com.eblan.launcher.feature.edit.model.EditUiState
import com.eblan.launcher.feature.edit.navigation.EditRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGridItemUseCase: GetGridItemUseCase,
    private val updateGridItemUseCase: UpdateGridItemUseCase,
) :
    ViewModel() {
    private val editRouteData = savedStateHandle.toRoute<EditRouteData>()

    private val _editUiState = MutableStateFlow<EditUiState>(EditUiState.Loading)

    val editUiState = _editUiState.onStart {
        getGridItem()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditUiState.Loading,
    )

    fun updateGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            updateGridItemUseCase(gridItem = gridItem)

            getGridItem()
        }
    }

    private fun getGridItem() {
        viewModelScope.launch {
            _editUiState.update {
                EditUiState.Success(
                    gridItem = getGridItemUseCase(id = editRouteData.id),
                )
            }
        }
    }
}
