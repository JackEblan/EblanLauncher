package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.EblanApplicationComponent

sealed interface EblanApplicationComponentUiState {
    data object Loading : EblanApplicationComponentUiState

    data class Success(
        val eblanApplicationComponent: EblanApplicationComponent,
    ) : EblanApplicationComponentUiState
}