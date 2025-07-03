package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanShortcutInfoDao
import com.eblan.launcher.data.room.entity.EblanShortcutInfoEntity
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanShortcutInfoRepository @Inject constructor(private val eblanShortcutInfoDao: EblanShortcutInfoDao) :
    EblanShortcutInfoRepository {
    override val eblanShortcutInfos =
        eblanShortcutInfoDao.getEblanShortcutInfoEntities().map { entities ->
            entities.map { entity ->
                entity.toEblanShortcutInfo()
            }
        }

    override suspend fun upsertEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.toEblanShortcutInfoEntity()
        }

        eblanShortcutInfoDao.upsertEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanShortcutInfo(eblanShortcutInfo: EblanShortcutInfo) {
        eblanShortcutInfoDao.upsertEblanShortcutInfoEntity(entity = eblanShortcutInfo.toEblanShortcutInfoEntity())
    }

    override suspend fun deleteEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.toEblanShortcutInfoEntity()
        }

        eblanShortcutInfoDao.deleteEblanShortcutInfoEntities(entities = entities)
    }

    private fun EblanShortcutInfo.toEblanShortcutInfoEntity(): EblanShortcutInfoEntity {
        return EblanShortcutInfoEntity(id = id)
    }

    private fun EblanShortcutInfoEntity.toEblanShortcutInfo(): EblanShortcutInfo {
        return EblanShortcutInfo(id = id)
    }
}