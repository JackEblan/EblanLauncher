package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import kotlinx.coroutines.flow.Flow

interface EblanAppWidgetProviderInfoRepository {
    val eblanAppWidgetProviderInfos: Flow<List<EblanAppWidgetProviderInfo>>

    suspend fun upsertEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>)

    suspend fun upsertEblanAppWidgetProviderInfo(eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo)

    suspend fun deleteEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>)
}