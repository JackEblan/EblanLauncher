package com.eblan.launcher.feature.settings.gestures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.gestures.model.GesturesSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestureSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) :
    ViewModel() {
    val gesturesSettingsUiState = userDataRepository.userData.map(
        GesturesSettingsUiState::Success,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GesturesSettingsUiState.Loading,
    )

    fun updateSwipeUp(gestureAction: GestureAction) {
        viewModelScope.launch {
            userDataRepository.updateSwipeUp(gestureAction = gestureAction)
        }
    }

    fun updateSwipeDown(gestureAction: GestureAction) {
        viewModelScope.launch {
            userDataRepository.updateSwipeDown(gestureAction = gestureAction)
        }
    }
}