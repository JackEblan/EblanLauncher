package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagCrossRefDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagCrossRefEntity
import com.eblan.launcher.domain.model.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagCrossRefRepository
import javax.inject.Inject

internal class DefaultEblanApplicationInfoTagCrossRefRepository @Inject constructor(private val eblanApplicationInfoTagCrossRefDao: EblanApplicationInfoTagCrossRefDao) :
    EblanApplicationInfoTagCrossRefRepository {
    override suspend fun insertEblanApplicationInfoTagCrossRef(eblanApplicationInfoTagCrossRef: EblanApplicationInfoTagCrossRef) {
        eblanApplicationInfoTagCrossRefDao.insertEblanApplicationInfoTagCrossRefEntity(
            entity = eblanApplicationInfoTagCrossRef.asEntity(),
        )
    }

    override suspend fun deleteEblanApplicationInfoTagCrossRef(
        componentName: String,
        serialNumber: Long,
        tagId: Long,
    ) {
        eblanApplicationInfoTagCrossRefDao.deleteEblanApplicationInfoTagCrossRefEntity(
            componentName = componentName,
            serialNumber = serialNumber,
            tagId = tagId,
        )
    }

    private fun EblanApplicationInfoTagCrossRef.asEntity(): EblanApplicationInfoTagCrossRefEntity =
        EblanApplicationInfoTagCrossRefEntity(
            componentName = componentName,
            serialNumber = serialNumber,
            id = id,
        )
}