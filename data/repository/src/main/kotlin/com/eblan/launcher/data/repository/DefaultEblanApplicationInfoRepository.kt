package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanApplicationInfoRepository @Inject constructor(private val eblanApplicationInfoDao: EblanApplicationInfoDao) :
    EblanApplicationInfoRepository {
    override val eblanApplicationInfos =
        eblanApplicationInfoDao.getEblanApplicationInfoEntities().map { entities ->
            entities.map { entity ->
                entity.toEblanApplicationInfo()
            }
        }

    override suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = eblanApplicationInfos.map { eblanApplicationInfo ->
            eblanApplicationInfo.toEblanApplicationInfoEntity()
        }

        eblanApplicationInfoDao.upsertEblanApplicationInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        eblanApplicationInfoDao.upsertEblanApplicationInfoEntity(entity = eblanApplicationInfo.toEblanApplicationInfoEntity())
    }

    override suspend fun getEblanApplicationInfo(packageName: String): EblanApplicationInfo? {
        return eblanApplicationInfoDao.getEblanApplicationInfoEntity(packageName = packageName)
            ?.toEblanApplicationInfo()
    }

    private fun EblanApplicationInfoEntity.toEblanApplicationInfo(): EblanApplicationInfo {
        return EblanApplicationInfo(
            id = id,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private fun EblanApplicationInfo.toEblanApplicationInfoEntity(): EblanApplicationInfoEntity {
        return EblanApplicationInfoEntity(
            id = id,
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }
}