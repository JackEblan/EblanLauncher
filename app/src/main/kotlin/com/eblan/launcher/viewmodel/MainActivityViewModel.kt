package com.eblan.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.model.MainActivityThemeSettings
import com.eblan.launcher.model.MainActivityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
    wallpaperManagerWrapper: WallpaperManagerWrapper,
) : ViewModel() {
    val uiState = combine(
        userDataRepository.userData,
        wallpaperManagerWrapper.getColorsChanged(),
    ) { userData, colorHints ->
        MainActivityUiState.Success(
            mainActivityThemeSettings = MainActivityThemeSettings(
                themeBrand = userData.generalSettings.themeBrand,
                darkThemeConfig = userData.generalSettings.darkThemeConfig,
                dynamicTheme = userData.generalSettings.dynamicTheme,
                hintSupportsDarkTheme = (colorHints?.and(wallpaperManagerWrapper.hintSupportsDarkText)) != 0,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainActivityUiState.Loading,
    )
}