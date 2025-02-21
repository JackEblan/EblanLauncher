package com.eblan.launcher.feature.home

import com.eblan.launcher.domain.model.GridItemPixel
import com.eblan.launcher.domain.model.ScreenDimension

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val gridItems: Map<Int, List<GridItemPixel>>,
        val screenDimension: ScreenDimension,
        val pageCount: Int,
    ) : HomeUiState
}