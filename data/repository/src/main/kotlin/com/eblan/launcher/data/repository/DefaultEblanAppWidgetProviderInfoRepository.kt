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

    override suspend fun upsertEblanAppWidgetProviderInfo(eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>) {
        val entities = withContext(Dispatchers.Default) {
            eblanAppWidgetProviderInfos.map { eblanAppWidgetProviderInfo ->
                eblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity()
            }
        }

        eblanAppWidgetProviderInfoDao.upsertEblanAppWidgetProviderInfoEntity(entities = entities)
    }

    private fun EblanAppWidgetProviderInfo.toEblanAppWidgetProviderInfoEntity(): EblanAppWidgetProviderInfoEntity {
        return EblanAppWidgetProviderInfoEntity(
            className = className,
            componentName = componentName,
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

fun EblanAppWidgetProviderInfoEntity.toEblanAppWidgetProviderInfo(): EblanAppWidgetProviderInfo {
    return EblanAppWidgetProviderInfo(
        className = className,
        componentName = componentName,
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