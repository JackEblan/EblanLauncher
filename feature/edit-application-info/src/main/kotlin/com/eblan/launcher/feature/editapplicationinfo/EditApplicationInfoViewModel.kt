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
package com.eblan.launcher.feature.editapplicationinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.feature.editapplicationinfo.navigation.EditApplicationInfoRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
) : ViewModel() {
    private val editApplicationInfoRouteData =
        savedStateHandle.toRoute<EditApplicationInfoRouteData>()

    private val _editApplicationInfoUiState =
        MutableStateFlow<EditApplicationInfoUiState>(EditApplicationInfoUiState.Loading)

    val editApplicationInfoUiState = _editApplicationInfoUiState.onStart {
        getEblanApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditApplicationInfoUiState.Loading,
    )

    fun updateEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        viewModelScope.launch {
            eblanApplicationInfoRepository.upsertEblanApplicationInfo(eblanApplicationInfo = eblanApplicationInfo)
        }
    }

    private fun getEblanApplicationInfo() {
        viewModelScope.launch {
            _editApplicationInfoUiState.update {
                EditApplicationInfoUiState.Success(
                    eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfo(
                        serialNumber = editApplicationInfoRouteData.serialNumber,
                        packageName = editApplicationInfoRouteData.packageName,
                    ),
                )
            }
        }
    }
}
