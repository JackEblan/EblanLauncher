package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.EblanApplicationInfoTagCrossRef

interface EblanApplicationInfoTagCrossRefRepository {
    suspend fun insertEblanApplicationInfoTagCrossRef(eblanApplicationInfoTagCrossRef: EblanApplicationInfoTagCrossRef)

    suspend fun deleteEblanApplicationInfoTagCrossRef(
        componentName: String,
        serialNumber: Long,
        tagId: Long,
    )
}