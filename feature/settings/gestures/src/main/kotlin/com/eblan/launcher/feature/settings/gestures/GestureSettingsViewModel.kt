package com.eblan.launcher.feature.settings.gestures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.GestureAction
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.gestures.model.GesturesSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestureSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    eblanApplicationInfoRepository: EblanApplicationInfoRepository,
) :
    ViewModel() {
    val gesturesSettingsUiState = combine(
        userDataRepository.userData,
        eblanApplicationInfoRepository.eblanApplicationInfos,
    ) { userData, eblanApplicationInfos ->
        GesturesSettingsUiState.Success(
            userData = userData,
            eblanApplicationInfos = eblanApplicationInfos.sortedBy { eblanApplicationInfo ->
                eblanApplicationInfo.label
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GesturesSettingsUiState.Loading,
    )

    fun updateDoubleTap(gestureAction: GestureAction) {
        viewModelScope.launch {
            userDataRepository.updateDoubleTap(gestureAction = gestureAction)
        }
    }

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