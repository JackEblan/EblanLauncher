package com.eblan.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.model.PinActivityUiState
import com.eblan.launcher.model.ThemeSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PinActivityViewModel @Inject constructor(
    userDataRepository: UserDataRepository,
) : ViewModel() {
    val pinActivityUiState = userDataRepository.userData.map { userData ->
        PinActivityUiState.Success(
            themeSettings = ThemeSettings(
                themeBrand = userData.generalSettings.themeBrand,
                darkThemeConfig = userData.generalSettings.darkThemeConfig,
                dynamicTheme = userData.generalSettings.dynamicTheme,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PinActivityUiState.Loading,
    )
}