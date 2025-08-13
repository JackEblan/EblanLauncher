package com.eblan.launcher.feature.edit.model

import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemSettings

sealed interface EditUiState {
    data object Loading : EditUiState

    data class Success(
        val gridItem: GridItem?,
        val gridItemSettings: GridItemSettings,
    ) :
        EditUiState
}