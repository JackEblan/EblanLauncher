package com.eblan.launcher.feature.settings.gestures.model

import com.eblan.launcher.domain.model.UserData

sealed interface GesturesSettingsUiState {
    data object Loading : GesturesSettingsUiState

    data class Success(val userData: UserData) : GesturesSettingsUiState
}