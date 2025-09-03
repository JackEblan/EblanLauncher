package com.eblan.launcher.feature.settings.gestures.model

import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.GestureSettings

sealed interface GesturesSettingsUiState {
    data object Loading : GesturesSettingsUiState

    data class Success(
        val gestureSettings: GestureSettings,
        val eblanApplicationInfos: List<EblanApplicationInfo>,
    ) : GesturesSettingsUiState
}