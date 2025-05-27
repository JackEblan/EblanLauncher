package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.EblanApplicationInfo

sealed interface ApplicationUiState {
    data object Loading : ApplicationUiState

    data class Success(
        val eblanApplicationInfos: List<EblanApplicationInfo>,
    ) : ApplicationUiState
}