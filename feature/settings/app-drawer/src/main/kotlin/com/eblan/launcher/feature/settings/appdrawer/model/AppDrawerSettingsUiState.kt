package com.eblan.launcher.feature.settings.appdrawer.model

import com.eblan.launcher.domain.model.UserData

sealed interface AppDrawerSettingsUiState {
    data object Loading : AppDrawerSettingsUiState

    data class Success(val userData: UserData) : AppDrawerSettingsUiState
}