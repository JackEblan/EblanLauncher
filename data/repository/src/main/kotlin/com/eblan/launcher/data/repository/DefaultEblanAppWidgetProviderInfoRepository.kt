package com.eblan.launcher.data.repository

import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.entity.EblanAppWidgetProviderInfoEntity
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DefaultEblanAppWidgetProviderInfoRepository @Inject constructor(private val eblanAppWidgetProviderInfoDao: EblanAppWidgetProviderInfoDao) :
    EblanAppWidgetProviderInfoRepository {
    override val eblanAppWidgetProviderInfos =
        eblanAppWidgetProviderInfoDao.getEblanAppWidgetProviderInfoEntities().map { entities ->
            entities.map { entity ->
                entity.asModel()
            }
        }

    override suspend fun upsertEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfo ->
            eblanAppWidgetProviderInfo.asEntity()
        }

        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntities(entities = entities)
    }

    override suspend fun upsertEblanAppWidgetProviderInfo(eblanAppWidgetProviderInfo: EblanAppWidgetProviderInfo) {
        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntity(entity = eblanAppWidgetProviderInfo.asEntity())
    }

    override suspend fun deleteEblanAppWidgetProviderInfos(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfo ->
            eblanAppWidgetProviderInfo.asEntity()
        }

        eblanAppWidgetProviderInfoDao.deleteEblanAppWidgetProviderInfoEntities(entities = entities)
    }

    private fun EblanAppWidgetProviderInfo.asEntity(): EblanAppWidgetProviderInfoEntity {
        return EblanAppWidgetProviderInfoEntity(
            className = className,
            componentName = componentName,
            configure = configure,
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

    private fun EblanAppWidgetProviderInfoEntity.asModel(): EblanAppWidgetProviderInfo {
        return EblanAppWidgetProviderInfo(
            className = className,
            componentName = componentName,
            configure = configure,
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