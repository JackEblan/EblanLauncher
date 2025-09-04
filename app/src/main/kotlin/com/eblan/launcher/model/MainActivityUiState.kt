package com.eblan.launcher.model

import com.eblan.launcher.domain.model.ApplicationTheme

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState

    data class Success(val applicationTheme: ApplicationTheme) : MainActivityUiState
}