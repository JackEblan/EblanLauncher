package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanIconPackInfo
import kotlinx.coroutines.flow.Flow

interface EblanIconPackInfoRepository {
    val eblanIconPackInfos: Flow<List<EblanIconPackInfo>>

    suspend fun upsertEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo): Long

    suspend fun deleteEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo)

    suspend fun getEblanIconPackInfo(packageName: String): EblanIconPackInfo?
}