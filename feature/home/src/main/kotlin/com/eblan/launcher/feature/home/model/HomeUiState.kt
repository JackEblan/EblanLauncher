package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItemsByPage

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val gridItemsByPage: GridItemsByPage
    ) : HomeUiState
}