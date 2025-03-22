package com.eblan.launcher.feature.home.model

import com.eblan.launcher.domain.model.GridItemByCoordinates

sealed interface GridItemUiState {
    data object Idle : GridItemUiState

    data class Success(val gridItemByCoordinates: GridItemByCoordinates?) : GridItemUiState
}