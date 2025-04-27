package com.eblan.launcher.feature.settings.settings.model

import com.eblan.launcher.domain.model.UserData

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(val userData: UserData) : SettingsUiState
}