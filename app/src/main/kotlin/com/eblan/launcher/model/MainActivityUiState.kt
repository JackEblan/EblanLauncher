package com.eblan.launcher.model

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Success(val mainActivityThemeSettings: MainActivityThemeSettings) : MainActivityUiState
}