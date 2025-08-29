package com.eblan.launcher.feature.settings.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
    val homeSettingsUiState = userDataRepository.userData.map(
        HomeSettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeSettingsUiState.Loading,
    )

    fun updateGrid(rows: Int, columns: Int) {
        viewModelScope.launch {
            userDataRepository.updateRows(rows = rows)

            userDataRepository.updateColumns(columns = columns)
        }
    }

    fun updateInfiniteScroll(infiniteScroll: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateInfiniteScroll(infiniteScroll = infiniteScroll)
        }
    }

    fun updateDockGrid(dockRows: Int, dockColumns: Int) {
        viewModelScope.launch {
            userDataRepository.updateDockRows(dockRows = dockRows)

            userDataRepository.updateDockColumns(dockColumns = dockColumns)
        }
    }

    fun updateDockHeight(dockHeight: Int) {
        viewModelScope.launch {
            userDataRepository.updateDockHeight(dockHeight = dockHeight)
        }
    }

    fun updateTextColor(textColor: TextColor) {
        viewModelScope.launch {
            userDataRepository.updateTextColor(textColor = textColor)
        }
    }

    fun updateWallpaperScroll(wallpaperScroll: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateWallpaperScroll(wallpaperScroll = wallpaperScroll)
        }
    }

    fun updateIconSize(iconSize: Int) {
        viewModelScope.launch {
            userDataRepository.updateIconSize(iconSize = iconSize)
        }
    }

    fun updateTextSize(textSize: Int) {
        viewModelScope.launch {
            userDataRepository.updateTextSize(textSize = textSize)
        }
    }

    fun updateShowLabel(showLabel: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateShowLabel(showLabel = showLabel)
        }
    }

    fun updateSingleLineLabel(singleLineLabel: Boolean) {
        viewModelScope.launch {
            userDataRepository.updateSingleLineLabel(singleLineLabel = singleLineLabel)
        }
    }
}