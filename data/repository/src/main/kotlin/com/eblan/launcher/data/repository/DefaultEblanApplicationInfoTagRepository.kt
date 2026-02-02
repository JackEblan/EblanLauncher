package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanApplicationInfoTagDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoTagEntity
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanApplicationInfoTagRepository @Inject constructor(private val eblanApplicationInfoTagDao: EblanApplicationInfoTagDao) :
    EblanApplicationInfoTagRepository {
    override val eblanApplicationInfoTags =
        eblanApplicationInfoTagDao.getEblanApplicationInfoTagEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun insertEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.insertEblanApplicationInfoTagEntity(entity = eblanApplicationInfoTag.asEntity())
    }

    override suspend fun updateEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.updateEblanApplicationInfoTagEntity(entity = eblanApplicationInfoTag.asEntity())
    }

    override suspend fun deleteEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        eblanApplicationInfoTagDao.deleteEblanApplicationInfoTagEntity(entity = eblanApplicationInfoTag.asEntity())
    }

    private fun EblanApplicationInfoTagEntity.asModel(): EblanApplicationInfoTag =
        EblanApplicationInfoTag(
            id = id,
            name = name,
        )

    private fun EblanApplicationInfoTag.asEntity(): EblanApplicationInfoTagEntity =
        EblanApplicationInfoTagEntity(
            id = id,
            name = name,
        )
}