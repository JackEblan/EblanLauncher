package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.entity.EblanAppWidgetProviderInfoEntity
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultEblanAppWidgetProviderInfoRepository @Inject constructor(private val eblanAppWidgetProviderInfoDao: EblanAppWidgetProviderInfoDao) :
    EblanAppWidgetProviderInfoRepository {
    override val eblanAppWidgetProviderInfos =
        eblanAppWidgetProviderInfoDao.getEblanAppWidgetProviderInfos().map { entities ->
            entities.map { entity ->
                entity.toEblanAppWidgetProviderInfo()
            }
        }

    override suspend fun upsertEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = withContext(Dispatchers.Default) {
            eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity()
            }
        }

        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanAppWidgetProviderInfo(eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo) {
        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntity(entity = eblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity())
    }

    override suspend fun deleteEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = withContext(Dispatchers.Default) {
            eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity()
            }
        }

        eblanAppWidgetProviderInfoDao.deleteEblanAppWidgetProviderInfoEntities(entities = entities)
    }

    private fun EblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity(): EblanAppWidgetProviderInfoEntity {
        return EblanAppWidgetProviderInfoEntity(
            className = className,
            componentName = componentName,
            packageName = packageName,
            targetCellWidth = targetCellWidth,
            targetCellHeight = targetCellHeight,
            minWidth = minWidth,
            minHeight = minHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            preview = preview,
            eblanApplicationInfo = eblanApplicationInfo,
        )
    }

    private fun EblanAppWidgetProviderInfoEntity.toEblanAppWidgetProviderInfo(): EblanAppWidgetProviderInfo {
        return EblanAppWidgetProviderInfo(
            className = className,
            componentName = componentName,
            packageName = packageName,
            targetCellWidth = targetCellWidth,
            targetCellHeight = targetCellHeight,
            minWidth = minWidth,
            minHeight = minHeight,
            resizeMode = resizeMode,
            minResizeWidth = minResizeWidth,
            minResizeHeight = minResizeHeight,
            maxResizeWidth = maxResizeWidth,
            maxResizeHeight = maxResizeHeight,
            preview = preview,
            eblanApplicationInfo = eblanApplicationInfo,
        )
    }
}