package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import kotlinx.coroutines.flow.Flow

interface EblanApplicationInfoTagRepository {
    val eblanApplicationInfoTags: Flow<List<EblanApplicationInfoTag>>
    
    suspend fun insertEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag)

    suspend fun updateEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag)

    suspend fun deleteEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag)
}