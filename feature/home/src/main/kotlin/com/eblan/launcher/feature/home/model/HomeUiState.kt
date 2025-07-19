package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.HomeData

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val homeData: HomeData,
    ) : HomeUiState
}