package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.UserData

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val gridItems: Map<Int, List<GridItem>>,
        val userData: UserData,
    ) : HomeUiState
}