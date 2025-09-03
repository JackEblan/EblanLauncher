package com.eblan.launcher.model

sealed interface PinActivityUiState {
    data object Loading : PinActivityUiState

    data class Success(val themeSettings: ThemeSettings) :
        PinActivityUiState
}