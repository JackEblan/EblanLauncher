package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemsByPage
import com.eblan.launcher.domain.model.UserData

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val gridItemsByPage: GridItemsByPage
    ) : HomeUiState
}