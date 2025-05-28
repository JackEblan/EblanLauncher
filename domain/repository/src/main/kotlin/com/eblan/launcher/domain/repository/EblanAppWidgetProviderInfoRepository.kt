package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import kotlinx.coroutines.flow.Flow

interface EblanAppWidgetProviderInfoRepository {
    val eblanAppWidgetProviderInfos: Flow<List<EblanAppWidgetProviderInfo>>

    suspend fun upsertEblanAppWidgetProviderInfo(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>)
}