package com.eblan.launcher.feature.settings.home.model

import com.eblan.launcher.domain.model.UserData

sealed interface HomeSettingsUiState {
    data object Loading : HomeSettingsUiState

    data class Success(val userData: UserData) : HomeSettingsUiState
}