package com.eblan.launcher.feature.settings.appdrawer.model

import com.eblan.launcher.domain.model.AppDrawerSettings

sealed interface AppDrawerSettingsUiState {
    data object Loading : AppDrawerSettingsUiState

    data class Success(val appDrawerSettings: AppDrawerSettings) : AppDrawerSettingsUiState
}