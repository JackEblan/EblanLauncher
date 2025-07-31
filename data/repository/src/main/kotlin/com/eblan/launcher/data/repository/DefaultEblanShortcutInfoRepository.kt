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
                entity.asModel()
            }
        }

    override suspend fun upsertEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.asEntity()
        }

        eblanShortcutInfoDao.upsertEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanShortcutInfo(eblanShortcutInfo: EblanShortcutInfo) {
        eblanShortcutInfoDao.upsertEblanShortcutInfoEntity(entity = eblanShortcutInfo.asEntity())
    }

    override suspend fun deleteEblanShortcutInfos(eblanShortcutInfos: List<EblanShortcutInfo>) {
        val entities = eblanShortcutInfos.map { eblanShortcutInfo ->
            eblanShortcutInfo.asEntity()
        }

        eblanShortcutInfoDao.deleteEblanShortcutInfoEntities(entities = entities)
    }

    override suspend fun getEblanShortcutInfo(id: String): EblanShortcutInfo? {
        return eblanShortcutInfoDao.getEblanShortcutInfoEntity(id = id)?.asModel()
    }

    private fun EblanShortcutInfo.asEntity(): EblanShortcutInfoEntity {
        return EblanShortcutInfoEntity(
            id = shortcutId,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            eblanApplicationInfo = eblanApplicationInfo,
        )
    }

    private fun EblanShortcutInfoEntity.asModel(): EblanShortcutInfo {
        return EblanShortcutInfo(
            shortcutId = id,
            packageName = packageName,
            shortLabel = shortLabel,
            longLabel = longLabel,
            icon = icon,
            eblanApplicationInfo = eblanApplicationInfo,
        )
    }
}