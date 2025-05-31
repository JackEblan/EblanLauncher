package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.entity.EblanApplicationInfoEntity
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultEblanApplicationInfoRepository @Inject constructor(private val eblanApplicationInfoDao: EblanApplicationInfoDao) :
    EblanApplicationInfoRepository {
    override val eblanApplicationInfos =
        eblanApplicationInfoDao.getEblanApplicationInfoEntities().map { entities ->
            entities.map { entity ->
                entity.toEblanApplicationInfo()
            }
        }

    override suspend fun upsertEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = withContext(Dispatchers.Default) {
            eblanApplicationInfos.map { eblanApplicationInfo ->
                eblanApplicationInfo.toEblanApplicationInfoEntity()
            }
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

    override suspend fun deleteEblanApplicationInfoByPackageName(packageName: String) {
        eblanApplicationInfoDao.deleteEblanApplicationInfoEntityByPackageName(packageName = packageName)
    }

    override suspend fun deleteEblanApplicationInfos(eblanApplicationInfos: List<EblanApplicationInfo>) {
        val entities = withContext(Dispatchers.Default) {
            eblanApplicationInfos.map { eblanApplicationInfo ->
                eblanApplicationInfo.toEblanApplicationInfoEntity()
            }
        }

        eblanApplicationInfoDao.deleteEblanApplicationInfoEntities(entities = entities)
    }

    private fun EblanApplicationInfoEntity.toEblanApplicationInfo(): EblanApplicationInfo {
        return EblanApplicationInfo(
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }

    private fun EblanApplicationInfo.toEblanApplicationInfoEntity(): EblanApplicationInfoEntity {
        return EblanApplicationInfoEntity(
            packageName = packageName,
            icon = icon,
            label = label,
        )
    }
}