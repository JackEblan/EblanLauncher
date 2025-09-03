package com.eblan.launcher.feature.settings.general.model

import com.eblan.launcher.domain.model.GeneralSettings

sealed interface GeneralSettingsUiState {
    data object Loading : GeneralSettingsUiState

    data class Success(val generalSettings: GeneralSettings) : GeneralSettingsUiState
}