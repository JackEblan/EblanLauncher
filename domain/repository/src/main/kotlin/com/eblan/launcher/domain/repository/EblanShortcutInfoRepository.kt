package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanShortcutInfo
import kotlinx.coroutines.flow.Flow

interface EblanShortcutInfoRepository {
    val eblanShortcutInfos: Flow<List<EblanShortcutInfo>>

    suspend fun upsertEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>)

    suspend fun upsertEblanShortcutInfo(eblanShortcutInfo: EblanShortcutInfo)

    suspend fun deleteEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>)

    suspend fun getEblanShortcutInfo(id: String): EblanShortcutInfo?
}