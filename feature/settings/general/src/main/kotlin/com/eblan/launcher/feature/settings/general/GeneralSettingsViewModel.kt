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
package com.eblan.launcher.feature.settings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.DarkThemeConfig
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.model.ThemeBrand
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.GetPackageManagerEblanIconPackInfosUseCase
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeneralSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    getPackageManagerEblanIconPackInfosUseCase: GetPackageManagerEblanIconPackInfosUseCase,
) :
    ViewModel() {
    val generalSettingsUiState = userDataRepository.userData.map { userData ->
        GeneralSettingsUiState.Success(generalSettings = userData.generalSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GeneralSettingsUiState.Loading,
    )

    private val _packageManagerEblanIconPackInfos = MutableStateFlow(emptyList<EblanIconPackInfo>())

    val packageManagerEblanIconPackInfos = _packageManagerEblanIconPackInfos.onStart {
        _packageManagerEblanIconPackInfos.update {
            getPackageManagerEblanIconPackInfosUseCase()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    val eblanIconPackInfos = eblanIconPackInfoRepository.eblanIconPackInfos.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateThemeBrand(themeBrand: ThemeBrand) {
        viewModelScope.launch {
            userDataRepository.updateThemeBrand(themeBrand = themeBrand)
        }
    }

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userDataRepository.updateDarkThemeConfig(darkThemeConfig = darkThemeConfig)
        }
    }

    fun updateDynamicTheme(dynamicTheme: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateDynamicTheme(dynamicTheme = dynamicTheme)
        }
    }

    fun updateIconPackInfoPackageName(iconPackInfoPackageName: String) {
        viewModelScope.launch {
            userDataRepository.updateIconPackInfoPackageName(iconPackInfoPackageName = iconPackInfoPackageName)
        }
    }

    fun deleteEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo) {
        viewModelScope.launch {
            eblanIconPackInfoRepository.deleteEblanIconPackInfo(eblanIconPackInfo = eblanIconPackInfo)
        }
    }
}
