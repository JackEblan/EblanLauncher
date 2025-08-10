package com.eblan.launcher.feature.settings.folder.model

import com.eblan.launcher.domain.model.UserData

sealed interface FolderSettingsUiState {
    data object Loading : FolderSettingsUiState

    data class Success(val userData: UserData) : FolderSettingsUiState
}