package com.eblan.launcher.data.repository

import com.eblan.launcher.data.repository.mapper.asEntity
import com.eblan.launcher.data.repository.mapper.asModel
import com.eblan.launcher.data.room.dao.EblanIconPackInfoDao
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class DefaultEblanIconPackInfoRepository @Inject constructor(private val eblanIconPackInfoDao: EblanIconPackInfoDao) :
    EblanIconPackInfoRepository {
    override val eblanIconPackInfos =
        eblanIconPackInfoDao.getEblanIconPackInfoEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo): Long {
        return eblanIconPackInfoDao.upsertEblanIconPackInfoEntity(entity = eblanIconPackInfo.asEntity())
    }

    override suspend fun deleteEblanIconPackInfo(eblanIconPackInfo: EblanIconPackInfo) {
        eblanIconPackInfoDao.deleteEblanIconPackInfoEntity(entity = eblanIconPackInfo.asEntity())
    }

    override suspend fun getEblanIconPackInfo(packageName: String): EblanIconPackInfo? {
        return eblanIconPackInfoDao.getEblanIconPackInfoEntity(packageName = packageName)?.asModel()
    }
}