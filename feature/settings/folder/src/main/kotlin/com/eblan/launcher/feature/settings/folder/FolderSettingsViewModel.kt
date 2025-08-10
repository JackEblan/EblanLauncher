package com.eblan.launcher.feature.settings.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.folder.model.FolderSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
    val folderSettingsUiState = userDataRepository.userData.map(
        FolderSettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FolderSettingsUiState.Loading,
    )

    fun updateFolderGrid(folderRows: Int, folderColumns: Int) {
        viewModelScope.launch {
            userDataRepository.updateFolderRows(folderRows = folderRows)

            userDataRepository.updateFolderColumns(folderColumns = folderColumns)
        }
    }
}