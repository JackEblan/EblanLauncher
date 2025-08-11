package com.eblan.launcher.feature.settings.appdrawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDrawerSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
    val appDrawerSettingsUiState = userDataRepository.userData.map(
        AppDrawerSettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppDrawerSettingsUiState.Loading,
    )

    fun updateAppDrawerGrid(
        appDrawerColumns: Int,
        appDrawerRowsHeight: Int,
    ) {
        viewModelScope.launch {
            userDataRepository.updateAppDrawerColumns(appDrawerColumns = appDrawerColumns)

            userDataRepository.updateAppDrawerRowsHeight(appDrawerRowsHeight = appDrawerRowsHeight)
        }
    }
}