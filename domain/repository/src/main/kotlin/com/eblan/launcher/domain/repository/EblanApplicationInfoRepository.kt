package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanApplicationInfo
import kotlinx.coroutines.flow.Flow

interface EblanApplicationInfoRepository {
    val eblanApplicationInfos: Flow<List<EblanApplicationInfo>>

    suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>)

    suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo)

    suspend fun getEblanApplicationInfo(packageName: String): EblanApplicationInfo?
}