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
package com.eblan.launcher.feature.settings.appdrawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDrawerSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
    val appDrawerSettingsUiState = userDataRepository.userData.map { userData ->
        AppDrawerSettingsUiState.Success(appDrawerSettings = userData.appDrawerSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppDrawerSettingsUiState.Loading,
    )

    fun updateAppDrawerGrid(
        appDrawerColumns: Int,
        appDrawerRowsHeight: Int,
    ) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerColumns(appDrawerColumns = appDrawerColumns)

            userDataRepository.updateAppDrawerRowsHeight(appDrawerRowsHeight = appDrawerRowsHeight)
        }
    }

    fun updateAppDrawerTextColor(textColor: TextColor) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerTextColor(textColor = textColor)
        }
    }

    fun updateAppDrawerIconSize(iconSize: Int) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerIconSize(iconSize = iconSize)
        }
    }

    fun updateAppDrawerTextSize(textSize: Int) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerTextSize(textSize = textSize)
        }
    }

    fun updateAppDrawerShowLabel(showLabel: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerShowLabel(showLabel = showLabel)
        }
    }

    fun updateAppDrawerSingleLineLabel(singleLineLabel: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerSingleLineLabel(singleLineLabel = singleLineLabel)
        }
    }
}
