package com.eblan.launcher.feature.home

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemOverlay
import com.eblan.launcher.domain.model.UserData

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val gridItems: Map<Int, List<GridItem>>,
        val userData: UserData,
    ) : HomeUiState
}

sealed interface GridItemOverlayUiState {
    data class Success(
        val gridItemOverlay: GridItemOverlay?,
    ) : GridItemOverlayUiState

    data object Idle : GridItemOverlayUiState
}

enum class SuccessUiState {
    Pager, Applications, Widgets
}