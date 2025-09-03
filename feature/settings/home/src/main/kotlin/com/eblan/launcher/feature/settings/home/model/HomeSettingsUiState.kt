package com.eblan.launcher.feature.settings.home.model

import com.eblan.launcher.domain.model.HomeSettings

sealed interface HomeSettingsUiState {
    data object Loading : HomeSettingsUiState

    data class Success(val homeSettings: HomeSettings) : HomeSettingsUiState
}