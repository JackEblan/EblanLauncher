package com.eblan.launcher.feature.settings.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(userDataRepository: UserDataRepository) :
    ViewModel() {
    val settingsUiState = userDataRepository.userData.map(
        HomeSettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeSettingsUiState.Loading,
    )
}