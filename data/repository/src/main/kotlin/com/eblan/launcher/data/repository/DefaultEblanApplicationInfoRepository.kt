package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanApplicationInfoRepository @Inject constructor(private val eblanApplicationInfoDao: EblanApplicationInfoDao) :
    EblanApplicationInfoRepository {
    override val eblanApplicationInfos =
        eblanApplicationInfoDao.getEblanApplicationInfoEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = eblanApplicationInfos.map { eblanApplicationInfo ->
            eblanApplicationInfo.asEntity()
        }

        eblanApplicationInfoDao.upsertEblanApplicationInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        eblanApplicationInfoDao.upsertEblanApplicationInfoEntity(entity = eblanApplicationInfo.asEntity())
    }

    override suspend fun getEblanApplicationInfo(packageName: String): EblanApplicationInfo? {
        return eblanApplicationInfoDao.getEblanApplicationInfoEntity(packageName = packageName)
            ?.asModel()
    }

    override suspend fun deleteEblanApplicationInfoByPackageName(packageName: String) {
        eblanApplicationInfoDao.deleteEblanApplicationInfoEntityByPackageName(packageName = packageName)
    }

    override suspend fun deleteEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = eblanApplicationInfos.map { eblanApplicationInfo ->
            eblanApplicationInfo.asEntity()
        }

        eblanApplicationInfoDao.deleteEblanApplicationInfoEntities(entities = entities)
    }
    
    private fun EblanApplicationInfo.asEntity(): EblanApplicationInfoEntity {
        return EblanApplicationInfoEntity(
            packageName = packageName,
            componentName = componentName,
            icon = icon,
            label = label,
        )
    }

    private fun EblanApplicationInfoEntity.asModel(): EblanApplicationInfo {
        return EblanApplicationInfo(
            packageName = packageName,
            componentName = componentName,
            icon = icon,
            label = label,
        )
    }
}