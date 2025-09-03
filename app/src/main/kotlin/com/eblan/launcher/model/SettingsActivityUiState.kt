package com.eblan.launcher.model

sealed interface SettingsActivityUiState {
    data object Loading : SettingsActivityUiState

    data class Success(val settingsActivityThemeSettings: SettingsActivityThemeSettings) :
        SettingsActivityUiState
}