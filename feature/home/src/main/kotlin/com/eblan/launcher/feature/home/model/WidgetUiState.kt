package com.eblan.launcher.feature.home.model

import android.appwidget.AppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo

sealed interface WidgetUiState {
    data object Loading : WidgetUiState

    data class Success(
        val appWidgetProviderInfos: Map<EblanApplicationInfo, List<AppWidgetProviderInfo>>,
    ) : WidgetUiState
}