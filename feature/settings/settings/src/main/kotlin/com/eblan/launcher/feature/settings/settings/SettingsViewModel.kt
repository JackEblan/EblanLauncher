package com.eblan.launcher.feature.settings.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.settings.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(userDataRepository: UserDataRepository) :
    ViewModel() {
    val settingsUiState = userDataRepository.userData.map(
        SettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState.Loading,
    )
}